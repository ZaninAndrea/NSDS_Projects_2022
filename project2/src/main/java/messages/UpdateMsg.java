package messages;

import java.io.Serializable;

import objects.Room;

public class UpdateMsg implements Serializable{
	Room r;

	public UpdateMsg(Room r) {
		this.r = r;
	}

	public Room getRoom() {
		return r;
	}
	
}
