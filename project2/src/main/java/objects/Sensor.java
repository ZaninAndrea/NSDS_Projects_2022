package objects;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;

public abstract class Sensor extends AbstractActor{
	
	protected int id;
	protected ActorSelection server;

}
