package objects;

import akka.actor.Props;
import messages.ConfigMsg;
import messages.GenerateMsg;
import messages.SensorMsg;

public class LightSensor extends Sensor{
	
	public LightSensor() {
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
		server.tell(new SensorMsg(value, super.id, MessageType.LIGHT_SENSOR), self());
	}
	
	void onConfigMessage(ConfigMsg msg) {
		id = msg.getId();
	}

	public static Props props() {
		return Props.create(LightSensor.class);
	}
}
