package org.polimi.nsds.project5.User;

public class User implements java.io.Serializable {
    // Data fields that will be serialized in the Kafka message
    public static final String topic = "users";

    public String name;
    public String address;

    public User(String name, String address){
        this.name = name;
        this.address = address;
    }
}
