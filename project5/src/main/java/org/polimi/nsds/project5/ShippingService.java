package org.polimi.nsds.project5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.polimi.nsds.project5.Order.Order;
import org.polimi.nsds.project5.Order.OrderDeserializer;
import org.polimi.nsds.project5.Order.OrderSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class ShippingService {
    //    private static final String kafkaBootstrapServers = "localhost:9092";
    private static final String kafkaBootstrapServers = "kafka:9093";


    private static KafkaConsumer<String, Order> setupConsumer(String groupId){
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

    private static KafkaProducer<String, Order> setupProducer(){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());

        final KafkaProducer<String, Order> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void main(String[] args) throws Exception{
        final KafkaProducer<String, Order> producer = setupProducer();
        String groupId = System.getenv().get("KAFKA_GROUP_ID") + RandomStringUtils.randomAlphabetic(15);
        System.out.println("Using group-id "+groupId);
        final KafkaConsumer<String, Order> consumer =  setupConsumer(groupId);
        ConcurrentHashMap<String, Order> cache = new ConcurrentHashMap<String, Order>();

        // Starts an http server to listen for order requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);
        server.createContext("/deliveries", new DeliveriesHandler(producer, cache));
        server.createContext("/orders", new OrdersHandler(cache));
        server.setExecutor(null);
        server.start();

        // Listen on the orders topic to keep an updated cache of the validated orders
        while (true) {
            final ConsumerRecords<String, Order> records = consumer.poll(Duration.of(1, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Order> record : records) {
                String key = record.key();
                Order order = record.value();

                // Ignore requested orders, add validated ones to the cache and remove the delivered ones
                if(order.status == Order.Status.VALIDATED){
                    System.out.println("Cached "+key);
                    cache.put(key, order);
                }else if(order.status == Order.Status.DELIVERED){
                    System.out.println("Removed "+key);
                    cache.remove(key);
                }
            }
        }
    }
    static class DeliveriesHandler implements HttpHandler {
        private KafkaProducer<String, Order> producer;
        private         ConcurrentHashMap<String, Order> cache;
        public DeliveriesHandler(KafkaProducer<String, Order> producer, ConcurrentHashMap<String, Order> cache){
            this.producer = producer;
            this.cache = cache;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Check that the request is a POST request or an OPTIONS request for CORS
            String method = t.getRequestMethod();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if(method.equals("OPTIONS")){
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                t.sendResponseHeaders(204, -1);
                return;
            }
            else if(!method.equals("POST")){
                String response = "Unsupported method";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Get details for the specified order from the cache
            String orderKey = new String(t.getRequestBody().readAllBytes());
            Order order = cache.get(orderKey);
            if (order == null){
                String response = "Order not found";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Generate new order message
            order.setStatus(Order.Status.DELIVERED);
            final ProducerRecord<String, Order> record = new ProducerRecord<>(Order.topic, orderKey, order);

            // Send message to Kafka and waits for acknowledgement
            try{
                final Future<RecordMetadata> future = this.producer.send(record);
                future.get();
            }catch(Exception e){
                e.printStackTrace();

                // Sends a failure HTTP response
                String response = "Failed to submit order update";
                t.sendResponseHeaders(500, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                return;
            }

            // Remove the order from the cache of the validated orders
            cache.remove(orderKey);

            // Sends a successful HTTP response
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class OrdersHandler implements HttpHandler {
        private         ConcurrentHashMap<String, Order> cache;
        public OrdersHandler(ConcurrentHashMap<String, Order> cache){
            this.cache = cache;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Check that the request is a GET or OPTIONS request
            String method = t.getRequestMethod();
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if(method.equals("OPTIONS")){
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                t.sendResponseHeaders(204, -1);
                return;
            }
            else if(!method.equals("GET")){
                String response = "Unsupported method";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // TODO FIX: iterating over the cache entries has an unspecified behaviour
            //  if the cache is modified while iterating
            ObjectMapper mapperObj = new ObjectMapper();
            String response = mapperObj.writeValueAsString(cache.entrySet());

            // Sends a successful HTTP response
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}