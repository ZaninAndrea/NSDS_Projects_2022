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
import messages.UpdateApplianceMsg;
import messages.UpdateMsg;
import objects.Appliance;
import objects.MessageType;
import objects.Room;
import objects.Status;

public class RoomManager extends AbstractActor {
	
	ActorSelection client;
	ArrayList<Room> roomList;
	ArrayList<Appliance> applianceList;
	
	public RoomManager() {
		roomList = new ArrayList<Room>();
		for(int i = 0; i<ServerMain.NUMBER_OF_ROOMS; i++) {
			roomList.add(new Room(i));
		}
		applianceList = new ArrayList<Appliance>();
		applianceList.add(new Appliance("TV", 250));
		applianceList.add(new Appliance("Oven", 750));
		applianceList.add(new Appliance("Washing Machine", 620));
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
		fOut.writeObject(applianceList);
		
		fOut.close();
	}
	
	@Override
	public void postRestart(Throwable reason) throws Exception {
		FileInputStream f = new FileInputStream("Snapshot.dat");
        ObjectInputStream fIn = new ObjectInputStream(f);
        
        roomList = (ArrayList<Room>) fIn.readObject();
        applianceList = (ArrayList<Appliance>) fIn.readObject();
        connect();
        
        System.out.println("...now restarted!");
        
        fIn.close();
	}
	
	void onSensorMessage(SensorMsg msg) {
		
		// light sensor messages
		if(msg.getType() == MessageType.LIGHT_SENSOR) {
			Room tmp = roomList.get(msg.getID());
			tmp.setLightStatus((msg.getValue() == 1 ? Status.ON : Status.OFF));
			send(tmp);
		} 
		// conditioning unit sensor messages
		if(msg.getType() == MessageType.HEAT_SENSOR){
			Room tmp = roomList.get(msg.getID());
			if(tmp.getHeaterStatus() != Status.MANUALLY_OFF)
				// setting the new heater status
				tmp.setHeaterStatus((msg.getValue() < tmp.getTargetTemp() ? Status.HEATING_MODE : Status.COOLING_MODE));
			tmp.setTemperature(msg.getValue());
			send(tmp);
		}
		// appliances sensor messages
		if(msg.getType() == MessageType.APPLIANCE) {
			Appliance tmp = applianceList.get(msg.getID());
			tmp.setIsON((msg.getValue() == 1 ? Status.ON : Status.OFF));
			send(Appliance.toString(applianceList));
		}
	}
	
	void onInitMessage(InitMsg msg) {
		connect();
		
		client.tell(new InitMsg(roomList, getTotalUsage(), Appliance.toString(applianceList)), self());
	}
	
	void onClientMessage(ClientMsg msg) throws Exception {
		// extracting the room with ID msg.getID()
		Room tmp = null;
		try {
			 tmp = roomList.get(msg.getID());
		}catch(Exception e) {
			throw new Exception("Actor fault!"); 
		}
		
		// controlling message type
		if(msg.getType() == MessageType.MANUALLY_SET_HEATER) {
			// setting room target temperature
			if(msg.getManHeater() == Status.MANUALLY_OFF)
				tmp.setHeaterStatus(msg.getManHeater());
			else
				tmp.setHeaterStatus((tmp.getTemperature() < tmp.getTargetTemp() ? Status.HEATING_MODE : Status.COOLING_MODE));
		} else {
			// setting the heater manually
			tmp.setTargetTemp(msg.getTargetTemp());
		}
		
		// controlling status
		if(tmp.getHeaterStatus() != Status.MANUALLY_OFF)
			// setting the new heater status
			tmp.setHeaterStatus((tmp.getTemperature() < tmp.getTargetTemp() ? Status.HEATING_MODE : Status.COOLING_MODE));

		// sending the reply to the the client
		send(tmp);
	}
	
	private void send(Room r) {
		client.tell(new UpdateMsg(r, getTotalUsage()), self());
	}
	
	private void send(String s) {
		client.tell(new UpdateApplianceMsg(s, getTotalUsage()), self());
	}
	
	private void connect() {
		String clientAddr = "akka://ClientSystem@127.0.0.1:6124/user/Client";
		client = getContext().actorSelection(clientAddr);
	}
	
	private int getTotalUsage() {
		int x = 0;
		for(Room r : roomList) {
			x += r.getPowerUsage();
		}
		
		for(Appliance a : applianceList) {
			if(a.getIsON() == Status.ON)
				x += a.getUsage();
		}
		return x;
	}
	
	public static Props props() {
		return Props.create(RoomManager.class);
	}
	
}
