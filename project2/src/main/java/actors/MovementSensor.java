package actors;

import akka.actor.Props;
import messages.ConfigMsg;
import messages.GenerateMsg;
import messages.SensorMsg;
import objects.MessageType;
import objects.Sensor;

public class MovementSensor extends Sensor{
	
	public MovementSensor() {
		String serverAddr = "akka://Server@127.0.0.1:6123/user/Server";
		server = getContext().actorSelection(serverAddr);
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(GenerateMsg.class, this::onGenerateMessage)
				.match(ConfigMsg.class, this::onConfigMessage).build();
	}
	
	void onGenerateMessage(GenerateMsg msg) {
		int value = (int) (Math.random()*2);
		//System.out.println("LightSensor "+super.roomID+" >> value generated: " + value);
		server.tell(new SensorMsg(value, super.roomID, MessageType.LIGHT_SENSOR), self());
	}
	
	void onConfigMessage(ConfigMsg msg) {
		roomID = msg.getRoomId();
	}

	public static Props props() {
		return Props.create(MovementSensor.class);
	}
}
