package messages;

import java.io.Serializable;

import objects.MessageType;

public class SensorMsg implements Serializable{
	
	private int id;
	private double value;
	private MessageType type;

	public SensorMsg(double value, int id, MessageType type) {
		this.value = value;
		this.id = id;
		this.type = type;
	}

	public double getValue() {
		return value;
	}
	
	public int getID() {
		return id;
	}

	public MessageType getType() {
		return type;
	}

}
