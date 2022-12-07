package org.polimi.nsds.project5.Order;

public class Order implements java.io.Serializable {
    // Data fields that will be serialized in the Kafka message
    public static final String topic = "orders";
    public long timestamp;
    public String[] items;


    public enum Status{
        REQUESTED,
        VALIDATED,
        DELIVERED,
        INVALID
    }

    public Status status;

    public Order(long timestamp, String[] items, Status status){
        this.timestamp = timestamp;
        this.items = items;
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setItems(String[] items) {
        this.items = items;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
