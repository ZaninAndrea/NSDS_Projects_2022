package org.polimi.nsds.project5.RequestedOrder;

import java.util.Date;

public class RequestedOrder implements java.io.Serializable {
    public static final String topic = "requested-orders";

    // Data fields that will be serialized in the Kafka message
    public long timestamp;
    public String[] items;

    public RequestedOrder(long timestamp, String[] items){
        this.timestamp = timestamp;
        this.items = items;
    }
}
