package org.polimi.nsds.project5.Order;

public class Order implements java.io.Serializable {
    // Data fields that will be serialized in the Kafka message
    public static final String topic = "orders";
    public long timestamp;
    public String[] items;
    public String address;
    public String customerEmail;


    public enum Status{
        REQUESTED,
        VALIDATED,
        DELIVERED,
        INVALID
    }

    public Status status;

    public Order(long timestamp, String[] items, Status status, String customerEmail){
        this.timestamp = timestamp;
        this.items = items;
        this.status = status;
        this.customerEmail = customerEmail;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setCustomerEmail(String email) {
        this.customerEmail = email;
    }

    public void setItems(String[] items) {
        this.items = items;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
