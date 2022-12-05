package org.polimi.nsds.project5;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrder;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrderDeserializer;
import org.polimi.nsds.project5.ValidatedOrder.ValidatedOrder;
import org.polimi.nsds.project5.ValidatedOrder.ValidatedOrderSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ValidationService {
    private static final String groupId = "validation-service";

    private static final String kafkaBootstrapServers = "localhost:9092";

    private static KafkaConsumer<String, RequestedOrder> setupOrdersConsumer(){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RequestedOrderDeserializer.class.getName());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, RequestedOrder> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(RequestedOrder.topic));

        return consumer;
    }

    private static KafkaProducer<String, ValidatedOrder> setupShippingProducer(String transactionalId){
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ValidatedOrderSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, ValidatedOrder> producer = new KafkaProducer<>(producerProps);
        producer.initTransactions();

        return producer;
    }

    public static void main(String[] args) {
        String transactionalId = "validation-service";
        KafkaConsumer<String, RequestedOrder> consumer = setupOrdersConsumer();
        KafkaProducer<String, ValidatedOrder> producer = setupShippingProducer(transactionalId);

        while (true) {
            final ConsumerRecords<String, RequestedOrder> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            producer.beginTransaction();
            for (final ConsumerRecord<String, RequestedOrder> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tTimestamp: " + record.value().timestamp+
                        "\tFirst item: " + record.value().items[0]
                );

                // TODO: validate order
                producer.send(new ProducerRecord<>(ValidatedOrder.topic, record.key(), new ValidatedOrder(record.value())));
            }


            // The producer manually commits the offsets for the consumer within the transaction
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, RequestedOrder>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }

            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();
        }
    }
}
