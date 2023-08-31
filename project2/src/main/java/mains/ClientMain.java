package mains;

import java.io.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import actors.ClientActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import messages.ConfigMsg;
import messages.GenerateMsg;

public class ClientMain {
	
	public static void main(String []args) throws Exception {
		
		Config conf = ConfigFactory.parseFile(new File("conf_Client"));
		final ActorSystem sys = ActorSystem.create("ClientSystem", conf);
		final ActorRef client = sys.actorOf(ClientActor.props(), "Client");
		
		client.tell(new ConfigMsg(), ActorRef.noSender());
		
		String s = (new BufferedReader(new InputStreamReader(System.in))).readLine();
		sys.terminate();
		
	}

}
