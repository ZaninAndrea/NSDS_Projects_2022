package messages;

public class ConfigMsg {
	private int roomId;

	public ConfigMsg(int roomId) {
		this.roomId = roomId;
	}
	
	public ConfigMsg() {
	}
	
	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
}
