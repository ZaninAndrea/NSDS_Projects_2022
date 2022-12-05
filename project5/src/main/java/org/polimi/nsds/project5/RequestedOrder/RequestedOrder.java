package org.polimi.nsds.project5.RequestedOrder;

public class RequestedOrder implements java.io.Serializable {
    public static final String topic = "requested-orders";

    // Data fields that will be serialized in the Kafka message
    public String timestamp;
    public String[] items;

    public RequestedOrder(String timestamp, String[] items){
        this.timestamp = timestamp;
        this.items = items;
    }
}
