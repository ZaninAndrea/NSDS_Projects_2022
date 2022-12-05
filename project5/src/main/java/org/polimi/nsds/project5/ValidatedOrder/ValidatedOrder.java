package org.polimi.nsds.project5.ValidatedOrder;

import org.polimi.nsds.project5.RequestedOrder.RequestedOrder;

import java.util.Date;

public class ValidatedOrder implements java.io.Serializable {
    public static final String topic = "validated-orders";

    // Data fields that will be serialized in the Kafka message
    public long timestamp;
    public String[] items;

    public ValidatedOrder(long timestamp, String[] items){
        this.timestamp = timestamp;
        this.items = items;
    }

    public ValidatedOrder(RequestedOrder order){
        this.timestamp = order.timestamp;
        this.items = order.items;
    }
}
