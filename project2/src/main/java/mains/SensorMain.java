package mains;

import java.io.File;
import java.util.ArrayList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import actors.RunnableSensor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import messages.ConfigMsg;
import objects.ApplianceSensor;
import objects.ConditioningSensor;
import objects.LightSensor;

public class SensorMain {

	public static void main(String[] args) throws Exception {

		Config conf = ConfigFactory.parseFile(new File("conf_Sensors"));
		final ActorSystem sys = ActorSystem.create("SensorSystem", conf);

		// generating sensor list
		final ArrayList<ActorRef> conditioningSensorList = new ArrayList<ActorRef>();
		final ArrayList<ActorRef> lightSensorList = new ArrayList<ActorRef>();
		final ArrayList<ActorRef> applianceSensorList = new ArrayList<ActorRef>();
		
		for (int i = 0; i < ServerMain.NUMBER_OF_ROOMS; i++) {

			// creating the new sensors
			ActorRef lightSensor = sys.actorOf(LightSensor.props(), "LightSensor" + (i));
			ActorRef conditioningSensor = sys.actorOf(ConditioningSensor.props(), "HeatSensor" + (i));

			lightSensor.tell(new ConfigMsg(i), ActorRef.noSender());
			conditioningSensor.tell(new ConfigMsg(i), ActorRef.noSender());

			// add the new sensors
			lightSensorList.add(lightSensor);
			conditioningSensorList.add(conditioningSensor);
		}
		
		for (int i = 0; i < ServerMain.NUMBER_OF_APPLIANCES; i++) {

			// creating the new sensors
			ActorRef applianceSensor = sys.actorOf(ApplianceSensor.props(), "ApplianceSensor" + (i));

			applianceSensor.tell(new ConfigMsg(i), ActorRef.noSender());

			// add the new sensors
			applianceSensorList.add(applianceSensor);
		}

		Thread movSensor = new Thread(new RunnableSensor(lightSensorList, 30000));
		Thread applianceSensor = new Thread(new RunnableSensor(applianceSensorList, 60000));
		Thread heatSensor = new Thread(new RunnableSensor(conditioningSensorList, 15000));
		
		movSensor.start();
		heatSensor.start();
		applianceSensor.start();

		movSensor.join();
		heatSensor.join();
		applianceSensor.join();
		
		sys.terminate();

	}

}
