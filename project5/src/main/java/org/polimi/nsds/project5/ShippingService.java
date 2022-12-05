package org.polimi.nsds.project5;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrder;
import org.polimi.nsds.project5.RequestedOrder.RequestedOrderDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class ShippingService {
    private static final String groupId = "shipping-service";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // consumer group id

        // Automatic saving of the consumer offsets on Kafka queue every autoCommitIntervalMs
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        // What to do when the consumer group listens to the queue for the first time:
        // Either earliest (start from first record) or latest (get only new records)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RequestedOrderDeserializer.class.getName());

        KafkaConsumer<String, RequestedOrder> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(RequestedOrder.topic));
        while (true) {
            final ConsumerRecords<String, RequestedOrder> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, RequestedOrder> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tTimestamp: " + record.value().timestamp+
                        "\tFirst item: " + record.value().items[0]
                );
            }
        }
    }
}
