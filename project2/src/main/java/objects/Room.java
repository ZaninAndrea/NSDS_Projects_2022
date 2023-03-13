package objects;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Room implements Serializable {
	
	private static final DecimalFormat df = new DecimalFormat("0.00");

	private int id;
	private double temperature, targetTemp;
	private Status lightStatus, HeaterStatus;

	public Room(int id) {
		this.id = id;
		this.temperature = 20.0;
		this.targetTemp = 20.0;
		this.lightStatus = Status.OFF;
		this.HeaterStatus = Status.OFF;
	}

	public double getTargetTemp() {
		return targetTemp;
	}

	public void setTargetTemp(double targetTemp) {
		this.targetTemp = targetTemp;
	}

	public int getId() {
		return id;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public Status getLightStatus() {
		return lightStatus;
	}

	public void setLightStatus(Status lightStatus) {
		this.lightStatus = lightStatus;
	}

	public Status getHeaterStatus() {
		return HeaterStatus;
	}

	public void setHeaterStatus(Status heaterStatus) {
		HeaterStatus = heaterStatus;
	}

	public String toString() {
		return "Room " + id + ": has temperature " + df.format(temperature) + ", target temperature " + df.format(targetTemp)
				+ ", has the lights " + lightStatus + " and the heater is " + HeaterStatus;
	}
}
