package org.polimi.nsds.project5;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.polimi.nsds.project5.Order.OrderDeserializer;
import org.polimi.nsds.project5.Order.OrderSerializer;
import org.polimi.nsds.project5.Order.Order;

public class OrdersService {
    private static final String kafkaBootstrapServers = "localhost:9092";

    private static KafkaConsumer<String, Order> setupConsumer(String groupId) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // consumer group id

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderDeserializer.class.getName());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // The consumer doesn't commit the offsets, because at each startup it must rebuild the cache
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(Order.topic));

        return consumer;
    }

    private static KafkaProducer<String, Order> setupProducer() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());

        final KafkaProducer<String, Order> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void main(String[] args) throws Exception {
        final KafkaProducer<String, Order> producer = setupProducer();
        // TODO: get the group if from the env
        final KafkaConsumer<String, Order> consumer = setupConsumer("orders-consumer");

        HashMap<String, Order> cache = new HashMap<String, Order>();

        // Starts an http server to listen for order requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/orders", new OrderHandler(producer, cache));
        server.setExecutor(null);
        server.start();


        // Listen on the orders topic to keep an updated cache of the validated orders
        while (true) {
            final ConsumerRecords<String, Order> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Order> record : records) {
                String key = record.key();
                Order order = record.value();
                System.out.println("Cached " + key + "as " + order.status.toString());
                cache.put(key, order);
            }
        }
    }

    static class OrderHandler implements HttpHandler {
        private KafkaProducer<String, Order> producer;
        private HashMap<String, Order> cache;

        public OrderHandler(KafkaProducer<String, Order> producer, HashMap<String, Order> cache) {
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
            } else if(method.equals("OPTIONS")){
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                t.sendResponseHeaders(204, -1);
                return;
            }else {
                String response = "Unsupported method";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        public void handleGet(HttpExchange t) throws IOException {
            ObjectMapper mapperObj = new ObjectMapper();
            String response = mapperObj.writeValueAsString(cache.entrySet());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


        public void handlePost(HttpExchange t) throws IOException {
            // Parse body of the request
            String body = new String(t.getRequestBody().readAllBytes());
            String[] items = body.split("\n");

            // Generate new order message
            final Order order = new Order(System.currentTimeMillis(), items, Order.Status.REQUESTED);
            UUID uuid = UUID.randomUUID();
            final ProducerRecord<String, Order> record = new ProducerRecord<>(Order.topic, uuid.toString(), order);

            // Send message to Kafka and waits for acknowledgement
            try {
                final Future<RecordMetadata> future = this.producer.send(record);
                future.get();
            } catch (Exception e) {
                e.printStackTrace();

                // Sends a failure HTTP response
                String response = "Failed to submit order";
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