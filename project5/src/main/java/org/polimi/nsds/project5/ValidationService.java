package org.polimi.nsds.project5;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.polimi.nsds.project5.Item.Item;
import org.polimi.nsds.project5.Item.ItemDeserializer;
import org.polimi.nsds.project5.Order.Order;
import org.polimi.nsds.project5.Order.OrderDeserializer;
import org.polimi.nsds.project5.Order.OrderSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class CacheManager implements Runnable
{
    ConcurrentHashMap<String, Item> cache;
    KafkaConsumer<String, Item> consumer;

    public CacheManager(ConcurrentHashMap<String, Item> cache, KafkaConsumer<String, Item> consumer){
        this.cache = cache;
        this.consumer = consumer;
    }
    public void run()
    {
        System.out.println("Listening for updates on a background thread");

        // Listen on the orders topic to keep an updated cache of the validated orders
        while (true) {
            final ConsumerRecords<String, Item> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Item> record : records) {
                String key = record.key();
                Item item = record.value();

                if(item.removed || !item.available){
                    System.out.println("Removed item "+item.name);
                    cache.remove(key);
                }else{
                    System.out.println("Cached item "+item.name);
                    cache.put(key, item);
                }
            }
        }
    }
}

public class ValidationService {
    private static final String groupId = "validation-service";

    private static final String kafkaBootstrapServers = "localhost:9092";

    private static KafkaConsumer<String, Order> setupOrdersConsumer(){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderDeserializer.class.getName());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(Order.topic));

        return consumer;
    }

    private static KafkaConsumer<String, Item> setupItemsConsumer(String groupId) {
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

    private static KafkaProducer<String, Order> setupShippingProducer(String transactionalId){
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, Order> producer = new KafkaProducer<>(producerProps);
        producer.initTransactions();

        return producer;
    }

    public static void main(String[] args) {
        String id = "validation-service"; // TODO: get from environment
        KafkaConsumer<String, Order> consumer = setupOrdersConsumer();
        KafkaConsumer<String, Item> itemsConsumer = setupItemsConsumer(id);
        KafkaProducer<String, Order> producer = setupShippingProducer(id);

        ConcurrentHashMap<String, Item> cache = new ConcurrentHashMap<>();

        // Run cache updating in a background thread
        CacheManager manager = new CacheManager(cache, itemsConsumer);
        Thread t1 =new Thread(manager);
        t1.start();

        // Listen for requested orders and validate them
        while (true) {
            final ConsumerRecords<String, Order> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            producer.beginTransaction();
            for (final ConsumerRecord<String, Order> record : records) {
                Order order = record.value();
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tTimestamp: " + order.timestamp+
                        "\tStatus: " + order.status
                );

                if(order.status == Order.Status.REQUESTED){
                    boolean valid = true;
                    for(String item : order.items){
                        if(!cache.containsKey(item)){
                            valid = false;
                            break;
                        }
                    }

                    if(valid){
                        order.setStatus(Order.Status.VALIDATED);
                    }else{
                        order.setStatus(Order.Status.INVALID);
                    }
                    producer.send(new ProducerRecord<>(Order.topic, record.key(), order));
                }
            }


            // The producer manually commits the offsets for the consumer within the transaction
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, Order>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }

            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();
        }
    }
}
