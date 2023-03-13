package actors;

import java.io.BufferedReader;
import java.util.ArrayList;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import messages.ConfigMsg;
import messages.GenerateMsg;
import messages.InitMsg;
import messages.UpdateMsg;
import objects.*;

public class ClientActor extends AbstractActor {
	
	
	GUI gui;
	ActorSelection server;
	BufferedReader t;
	ArrayList<Room> roomList;
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ConfigMsg.class, this::onConfigMessage)
				.match(GenerateMsg.class, this::onGenerateMessage)
				.match(InitMsg.class, this::onInitMessage)
				.match(UpdateMsg.class, this::onUpdateMessage).build();
	}
	
	void onGenerateMessage(GenerateMsg msg) {
		
	}
	
	void onInitMessage(InitMsg msg) {
		roomList = msg.getRoomList();
		gui = new GUI(roomList, this);
	}
	
	void onUpdateMessage(UpdateMsg msg) {
		Room tmp = msg.getRoom();
		roomList.set(tmp.getId(), tmp);
		
		gui.update(tmp);
	}
	
	void onConfigMessage(ConfigMsg msg) {
		String serverAddr = "akka://Server@127.0.0.1:6123/user/Server";
		server = getContext().actorSelection(serverAddr);
		
		server.tell(new InitMsg(null), self());
	}
	
	public static Props props() {
		return Props.create(ClientActor.class);
	}
	
	public ActorSelection getServer() {
		return server;
	}

	public ArrayList<Room> getRoomList() {
		return roomList;
	}
}
