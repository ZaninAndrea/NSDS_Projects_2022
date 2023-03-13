package actors;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import mains.ServerMain;
import messages.ClientMsg;
import messages.InitMsg;
import messages.SensorMsg;
import messages.UpdateMsg;
import objects.MessageType;
import objects.Room;
import objects.Status;

public class RoomManager extends AbstractActor {
	
	ActorSelection client;
	ArrayList<Room> roomList;
	
	public RoomManager() {
		roomList = new ArrayList<Room>();
		for(int i = 0; i<ServerMain.NUMBER_OF_ROOMS; i++) {
			roomList.add(new Room(i));
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SensorMsg.class, this::onSensorMessage)
				.match(InitMsg.class, this::onInitMessage)
				.match(ClientMsg.class, this::onClientMessage).build();
	}
	
	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws IOException {
		System.out.print("Preparing to restart...");
		FileOutputStream f = new FileOutputStream("Snapshot.dat");
		ObjectOutputStream fOut = new ObjectOutputStream(f);
		
		fOut.writeObject(roomList);
		
		fOut.close();
	}
	
	@Override
	public void postRestart(Throwable reason) throws Exception {
		FileInputStream f = new FileInputStream("Snapshot.dat");
        ObjectInputStream fIn = new ObjectInputStream(f);
        
        roomList = (ArrayList<Room>) fIn.readObject();
        connect();
        
        System.out.println("...now restarted!");
        
        fIn.close();
	}
	
	void onSensorMessage(SensorMsg msg) {
		// extracting the room with ID msg.getID()
		Room tmp = roomList.get(msg.getID());
		
		if(msg.getType() == MessageType.LIGHT_SENSOR) {
			// setting the new light status
			tmp.setLightStatus((msg.getValue() == 1 ? Status.ON : Status.OFF));
			//System.out.println("RoomManager >> changing light status of room "+msg.getID()+" to "+tmp.getLightStatus());
		} else {
			// controlling status
			if(tmp.getHeaterStatus() != Status.MANUALLY_ON)
				// setting the new heater status
				tmp.setHeaterStatus((msg.getValue() < tmp.getTargetTemp() ? Status.ON : Status.OFF));
			tmp.setTemperature(msg.getValue());
			//System.out.println("RoomManager >> updating temperature of room "+msg.getID()+" to "+msg.getValue());
			//System.out.println("RoomManager >> heater of room "+msg.getID()+" setted to "+tmp.getHeaterStatus());
		}
		
		// sending the update to the the client
		send(tmp);
	}
	
	void onInitMessage(InitMsg msg) {
		connect();
		
		client.tell(new InitMsg(roomList), self());
	}
	
	void onClientMessage(ClientMsg msg) throws Exception {
		// extracting the room with ID msg.getID()
		Room tmp = null;
		try {
			 tmp = roomList.get(msg.getID());
		}catch(Exception e) {
			throw new Exception("Actor fault!"); 
		}
		
		if(msg.getType() == MessageType.MANUALLY_SET_HEATER) {
			// setting room target temperature
			tmp.setHeaterStatus(msg.getManHeater());
		} else {
			// setting the heater manually
			tmp.setTargetTemp(msg.getTargetTemp());
			//System.out.println("RoomManager >> changed target temperature of room "+msg.getID()+" to "+tmp.getTargetTemp());
		}
		
		// controlling status
		if(tmp.getHeaterStatus() != Status.MANUALLY_ON)
			// setting the new heater status
			tmp.setHeaterStatus((tmp.getTemperature() < tmp.getTargetTemp() ? Status.ON : Status.OFF));
		//System.out.println("RoomManager >> heater of room "+msg.getID()+" setted to "+tmp.getHeaterStatus());

		// sending the reply to the the client
		send(tmp);
	}
	
	private void send(Room r) {
		client.tell(new UpdateMsg(r), self());
	}
	
	private void connect() {
		String clientAddr = "akka://ClientSystem@127.0.0.1:6124/user/Client";
		client = getContext().actorSelection(clientAddr);
	}
	
	public static Props props() {
		return Props.create(RoomManager.class);
	}
	
}
