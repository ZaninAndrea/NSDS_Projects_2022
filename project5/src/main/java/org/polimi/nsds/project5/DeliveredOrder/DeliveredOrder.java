package org.polimi.nsds.project5.DeliveredOrder;

import org.polimi.nsds.project5.ValidatedOrder.ValidatedOrder;

import java.util.Date;

public class DeliveredOrder implements java.io.Serializable {
    public static final String topic = "delivered-orders";

    // Data fields that will be serialized in the Kafka message
    public long timestamp;
    public String[] items;

    public DeliveredOrder(long timestamp, String[] items){
        this.timestamp = timestamp;
        this.items = items;
    }

    public DeliveredOrder(ValidatedOrder order){
        this.timestamp = order.timestamp;
        this.items = order.items;
    }
}
