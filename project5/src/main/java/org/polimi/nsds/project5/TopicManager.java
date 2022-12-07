package org.polimi.nsds.project5;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.polimi.nsds.project5.Item.Item;

import java.util.Collections;
import java.util.Properties;

public class TopicManager {

    private static final int topicPartitions = 32;
    private static final short replicationFactor = 1;

    private static final String kafkaBootstrapServers = "localhost:9092";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        AdminClient adminClient = AdminClient.create(props);

        NewTopic newTopic = new NewTopic(Item.topic, topicPartitions, replicationFactor);
        CreateTopicsResult createResult = adminClient.createTopics(Collections.singletonList(newTopic));
        createResult.all().get();
    }
}
