package messages;

import java.io.Serializable;

import objects.MessageType;
import objects.Status;

public class ClientMsg implements Serializable{
	
	private int roomID;
	private double targetTemp;
	private Status manHeater;
	private MessageType type;
	
	public ClientMsg(int roomId, Status manHeater, MessageType type) {
		this.manHeater = manHeater;
		this.type = type;
		this.roomID = roomId;
	}
	
	public ClientMsg(int roomId, double targetTemp, MessageType type) {
		this.targetTemp = targetTemp;
		this.type = type;
		this.roomID = roomId;
	}
	
	public MessageType getType() {
		return type;
	}

	public double getTargetTemp() {
		return targetTemp;
	}
	
	public Status getManHeater() {
		return manHeater;
	}

	public int getID() {
		return roomID;
	}

}
