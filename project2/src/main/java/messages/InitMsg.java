package messages;

import java.io.Serializable;
import java.util.ArrayList;

import objects.Room;

public class InitMsg implements Serializable{
	
	private ArrayList<Room> roomList;
	
	public InitMsg(ArrayList<Room> roomList) {
		super();
		this.roomList = roomList;
	}

	public ArrayList<Room> getRoomList() {
		return roomList;
	}
	
}
