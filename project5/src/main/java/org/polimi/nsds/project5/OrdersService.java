package org.polimi.nsds.project5;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrderSerializer;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrder;

public class OrdersService {
    private static final String kafkaBootstrapServers = "localhost:9092";

    private static KafkaProducer<String, RequestedOrder> setupProducer(){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RequestedOrderSerializer.class.getName());

        final KafkaProducer<String, RequestedOrder> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static void main(String[] args) throws Exception{
        final KafkaProducer<String, RequestedOrder> producer = setupProducer();

        // Starts an http server to listen for order requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/order", new OrderHandler(producer));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    static class OrderHandler implements HttpHandler {
        private KafkaProducer<String, RequestedOrder> producer;
        public OrderHandler(KafkaProducer<String, RequestedOrder> producer){
            this.producer = producer;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Check that the request is a POST request
            String method = t.getRequestMethod();
            if(!method.equals("POST")){
                String response = "Unsupported method";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Parse body of the request
            String body = new String(t.getRequestBody().readAllBytes());
            String[] items = body.split("\n");

            // Generate new order message
            final RequestedOrder order = new RequestedOrder(System.currentTimeMillis(), items);
            UUID uuid = UUID.randomUUID();
            final ProducerRecord<String, RequestedOrder> record = new ProducerRecord<>(RequestedOrder.topic, uuid.toString(), order);

            // Send message to Kafka and waits for acknowledgement
            try{
                final Future<RecordMetadata> future = this.producer.send(record);
                future.get();
            }catch(Exception e){
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