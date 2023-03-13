package messages;

import java.io.Serializable;

import objects.MessageType;

public class SensorMsg implements Serializable{
	
	private int roomID;
	private double value;
	private MessageType type;

	public SensorMsg(double value, int id, MessageType type) {
		this.value = value;
		this.roomID = id;
		this.type = type;
	}

	public double getValue() {
		return value;
	}
	
	public int getID() {
		return roomID;
	}

	public MessageType getType() {
		return type;
	}

}
