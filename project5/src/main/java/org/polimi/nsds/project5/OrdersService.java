package org.polimi.nsds.project5;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    public static void main(String[] args) {
        System.out.println("Initializing");
        final KafkaProducer<String, RequestedOrder> producer = setupProducer();

        final RequestedOrder order = new RequestedOrder("now", new String[]{"prova"});

        final ProducerRecord<String, RequestedOrder> record = new ProducerRecord<>(RequestedOrder.topic, "1", order);
        System.out.println("Sending message");
        final Future<RecordMetadata> future = producer.send(record);

        System.out.println("Waiting for ack");

        try {
            RecordMetadata ack = future.get();
            System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        producer.close();
    }
}