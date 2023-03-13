package mains;

import static akka.pattern.Patterns.ask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import actors.RoomManager;
import actors.Supervisor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class ServerMain {
	
	public static final int NUMBER_OF_MESSAGES = 5;
	public static final int NUMBER_OF_ROOMS = 4;

	public static void main(String[] args) throws Exception {
		//scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		
		Config conf = ConfigFactory.parseFile(new File("conf_Server"));
		final ActorSystem sys = ActorSystem.create("Server", conf);
		
		// creating supervisor for the room manager
		final ActorRef supervisor = sys.actorOf(Supervisor.props(), "supervisor");
		
		// generating actors
		final ActorRef server = sys.actorOf(RoomManager.props(), "Server");;
		
		// adding the actor to the supervisor
		ask(supervisor, server, 5000);
		//scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, server, 5000);
		//server = (ActorRef) waitingForCounter.result(timeout, null);
		
		String s = (new BufferedReader(new InputStreamReader(System.in))).readLine();
		
		sys.terminate();

	}

}
