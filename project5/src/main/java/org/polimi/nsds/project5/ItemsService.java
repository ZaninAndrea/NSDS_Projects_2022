package org.polimi.nsds.project5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.polimi.nsds.project5.Item.Item;
import org.polimi.nsds.project5.Item.ItemDeserializer;
import org.polimi.nsds.project5.Item.ItemSerializer;
import org.polimi.nsds.project5.Order.Order;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;

public class ItemsService {
    private static final String kafkaBootstrapServers = "localhost:9092";

    private static KafkaConsumer<String, Item> setupConsumer(String groupId) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // consumer group id

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ItemDeserializer.class.getName());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // The consumer doesn't commit the offsets, because at each startup it must rebuild the cache
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, Item> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(Item.topic));

        return consumer;
    }

    private static KafkaProducer<String, Item> setupProducer() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ItemSerializer.class.getName());

        final KafkaProducer<String, Item> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void main(String[] args) throws Exception {
        final KafkaProducer<String, Item> producer = setupProducer();
        // TODO: get the group if from the env
        final KafkaConsumer<String, Item> consumer = setupConsumer("item-consumer");

        HashMap<String, Item> cache = new HashMap<>();

        // Starts an http server to listen for order requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8003), 0);
        server.createContext("/items", new ItemHandler(producer, cache));
        server.setExecutor(null);
        server.start();
        System.out.println("Server listening");

        // Listen on the orders topic to keep an updated cache of the validated orders
        while (true) {
            final ConsumerRecords<String, Item> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            System.out.println("Polled records: "+records.count());
            for (final ConsumerRecord<String, Item> record : records) {
                String key = record.key();
                Item item = record.value();

                if(item.removed){
                    cache.remove(key);
                }else{
                    cache.put(key, item);
                }
            }
        }
    }

    static class ItemHandler implements HttpHandler {
        private KafkaProducer<String, Item> producer;
        private HashMap<String, Item> cache;

        public ItemHandler(KafkaProducer<String, Item> producer, HashMap<String, Item> cache) {
            this.producer = producer;
            this.cache = cache;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            // Check that the request is a POST request
            String method = t.getRequestMethod();
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if (method.equals("GET")) {
                handleGet(t);
            } else if (method.equals("POST")) {
                handlePost(t);
            } else if (method.equals("DELETE")) {
                handleDelete(t);
            } else if (method.equals("OPTIONS")) {
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                t.sendResponseHeaders(204, -1);
                return;
            } else {
                String response = "Unsupported method";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        public void handleGet(HttpExchange t) throws IOException {
            // Serialize cached items to JSON
            ObjectMapper mapperObj = new ObjectMapper();
            String response = mapperObj.writeValueAsString(cache.entrySet());

            // Send response
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        public void handleDelete(HttpExchange t) throws IOException {
            // Parse body of the request
            String name = new String(t.getRequestBody().readAllBytes());
            final Item item = new Item(name, false, true);
            final ProducerRecord<String, Item> record = new ProducerRecord<>(Item.topic, name, item);

            // Send message to Kafka and waits for acknowledgement
            try {
                final Future<RecordMetadata> future = this.producer.send(record);
                future.get();
            } catch (Exception e) {
                e.printStackTrace();

                // Sends a failure HTTP response
                String response = "Failed to delete item";
                t.sendResponseHeaders(500, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                return;
            }


            // Sends a successful HTTP response
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


        public void handlePost(HttpExchange t) throws IOException {
            // Parse body of the request
            String body = new String(t.getRequestBody().readAllBytes());
            String[] lines = body.split("\n");
            if (lines.length != 2){
                String response = "The body should contain two lines: the first with the item name and the second with the availability";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                return;
            }

            String name = lines[0];
            String rawAvailability = lines[1];
            boolean availability;
            if (rawAvailability.equals("true")){
                availability = true;
            } else if (rawAvailability.equals("false")) {
                availability = false;
            }else{
                String response = "Availability should be either true or false, got "+rawAvailability;
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                return;
            }

            // Generate new order message
            final Item item = new Item(name, availability, false);
            final ProducerRecord<String, Item> record = new ProducerRecord<>(Item.topic, name, item);

            // Send message to Kafka and waits for acknowledgement
            try {
                System.out.println("Waiting for ack");
                final Future<RecordMetadata> future = this.producer.send(record);
                future.get();
            } catch (Exception e) {
                e.printStackTrace();

                // Sends a failure HTTP response
                String response = "Failed to submit item";
                t.sendResponseHeaders(500, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                return;
            }


            // Sends a successful HTTP response
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}