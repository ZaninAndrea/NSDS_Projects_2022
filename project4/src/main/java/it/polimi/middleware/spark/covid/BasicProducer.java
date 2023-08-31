package it.polimi.middleware.spark.covid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

public class BasicProducer {
    private static final String defaultTopic = "topicCovid";
    private static final int waitBetweenMsgs = 5 * 1000;
    private static final boolean waitAck = false;
    private static final String serverAddr = "localhost:9092";

    private static final String filePath = "./files/covid/WHO-COVID-19-global-data-ordered.csv";
    private static final int countryCount = 237; 
    private static final int dayCount = 1315;

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Parse the csv source file
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        }   catch (final IOException e) {
            e.printStackTrace();
        }


        // Send all the records to the kafka topic
        for (int i=0;i<dayCount;i++) {
            for(int j=0; j<countryCount;j++) {  //send all the row of current day
                final String topic = defaultTopic;
                final List<String> nowReading = records.get(i*countryCount + j);

                // Creating a JSON object with the actual row 
                JSONObject newRow = new JSONObject();
                newRow.put("dayCount", Integer.valueOf(nowReading.get(0)));
                newRow.put("dateReported", nowReading.get(1));
                newRow.put("countryCode", nowReading.get(2));
                newRow.put("countryName", nowReading.get(3));
                newRow.put("countryArea", nowReading.get(4));
                newRow.put("newCases", Integer.valueOf(nowReading.get(5)));
                newRow.put("cumulativeCases", Integer.valueOf(nowReading.get(6)));
                newRow.put("newDeath", Integer.valueOf(nowReading.get(7)));
                newRow.put("cumulativeDeath", Integer.valueOf(nowReading.get(8)));

                System.out.println(newRow);

                final ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, newRow.toString());
                final Future<RecordMetadata> future = producer.send(record);

                if (waitAck) {
                    try {
                        RecordMetadata ack = future.get();
                        System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                    } catch (InterruptedException | ExecutionException e1) {
                        e1.printStackTrace();
                    }
                }

            }
            
            try {   // Wait some time before sending another day
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

        }
        producer.close();
    }
}