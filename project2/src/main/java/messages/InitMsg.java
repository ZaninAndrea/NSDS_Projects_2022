package messages;

import java.io.Serializable;
import java.util.ArrayList;

import objects.Room;

public class InitMsg implements Serializable{
	
	private ArrayList<Room> roomList;
	private int usage;
	private String appliances;
	
	public InitMsg(ArrayList<Room> roomList, int usage, String appliances) {
		this.roomList = roomList;
		this.usage = usage;
		this.appliances = appliances;
	}

	public ArrayList<Room> getRoomList() {
		return roomList;
	}
	
	public int getUsage() {
		return usage;
	}
	
	public String getAppliances() {
		return appliances;
	}
	
}
