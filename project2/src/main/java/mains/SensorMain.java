package mains;

import java.io.File;
import java.util.ArrayList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import actors.HeatSensor;
import actors.MovementSensor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import messages.ConfigMsg;
import messages.GenerateMsg;

public class SensorMain {

	public static void main(String[] args) throws Exception {

		Config conf = ConfigFactory.parseFile(new File("conf_Sensors"));
		final ActorSystem sys = ActorSystem.create("SensorSystem", conf);

		// generating sensor list
		final ArrayList<ActorRef> sensorList = new ArrayList<ActorRef>();
		for (int i = 0; i < ServerMain.NUMBER_OF_ROOMS; i++) {

			// creating the new sensors
			ActorRef lightSensor = sys.actorOf(MovementSensor.props(), "LightSensor" + (i));
			ActorRef heatSensor = sys.actorOf(HeatSensor.props(), "HeatSensor" + (i));

			lightSensor.tell(new ConfigMsg(i), ActorRef.noSender());
			heatSensor.tell(new ConfigMsg(i), ActorRef.noSender());

			// add the new sensors
			sensorList.add(lightSensor);
			sensorList.add(heatSensor);
		}

		// sending messages
		for (int i = 0; i < ServerMain.NUMBER_OF_MESSAGES; i++) {
			for (ActorRef a : sensorList) {
				a.tell(new GenerateMsg(), ActorRef.noSender());
			}
			System.out.println("\n>> the sensors detected something \n\n");
			Thread.sleep(30000);
		}

		sys.terminate();

	}

}
