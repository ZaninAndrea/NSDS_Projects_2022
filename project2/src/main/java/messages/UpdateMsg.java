package messages;

import java.io.Serializable;

import objects.Room;

public class UpdateMsg implements Serializable{
	Room r;
	int usage;

	public UpdateMsg(Room r, int usage) {
		this.r = r;
		this.usage = usage;
	}

	public Room getRoom() {
		return r;
	}
	
	public int getUsage() {
		return usage;
	}
	
}
