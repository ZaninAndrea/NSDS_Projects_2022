package actors;

import java.util.ArrayList;

import akka.actor.ActorRef;
import mains.ServerMain;
import messages.GenerateMsg;

public class RunnableSensor implements Runnable{
	
	final ArrayList<ActorRef> sensorList;
	int interval;

	public RunnableSensor(ArrayList<ActorRef> sensorList, int interval) {
		this.sensorList = sensorList;
		this.interval = interval;
	}
	
	public void run(){
		while(true) {
			// sending messages
			for (int i = 0; i < ServerMain.NUMBER_OF_MESSAGES; i++) {
				for (ActorRef a : sensorList) {
					a.tell(new GenerateMsg(), ActorRef.noSender());
				}
				if(interval == 30000)
					System.out.println("\n>> heat sensors detection \n\n");
				if(interval == 15000)
					System.out.println("\n>> light sensors detection \n\n");
				if(interval == 60000)
					System.out.println("\n>> appliance sensors detection \n\n");
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
