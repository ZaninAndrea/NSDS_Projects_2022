package org.polimi.nsds.project5.Item;

public class Item implements java.io.Serializable {
    public static final String topic = "items";

    public String name;
    public boolean available;
    public boolean removed;

    public Item(String name, boolean available, boolean removed){
        this.name = name;
        this.available = available;
        this.removed = removed;
    }
}
