package org.polimi.nsds.project5;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.polimi.nsds.project5.Item.Item;
import org.polimi.nsds.project5.Order.Order;
import org.polimi.nsds.project5.User.User;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class TopicManager {

    private static final int topicPartitions = 32;
    private static final short replicationFactor = 1;

    private static final String kafkaBootstrapServers = "localhost:9092";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        AdminClient adminClient = AdminClient.create(props);

        LinkedList<NewTopic> topics = new LinkedList<>();
        topics.add(new NewTopic(Item.topic, topicPartitions, replicationFactor));
        topics.add(new NewTopic(Order.topic, topicPartitions, replicationFactor));
        topics.add(new NewTopic(User.topic, topicPartitions, replicationFactor));

        CreateTopicsResult createResult = adminClient.createTopics(topics);
        createResult.all().get();
    }
}
