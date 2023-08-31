package objects;

import java.awt.peer.LightweightPeer;
import java.io.Serializable;
import java.text.DecimalFormat;

public class Room implements Serializable {
	
	private static final DecimalFormat df = new DecimalFormat("0.00");

	private int id, powerUsage;
	private double temperature, targetTemp;
	private Status lightStatus, conditioningUnitStatus;

	public Room(int id) {
		this.id = id;
		this.temperature = 20.0;
		this.targetTemp = 20.0;
		this.lightStatus = Status.OFF;
		this.conditioningUnitStatus = Status.COOLING_MODE;
		this.powerUsage = 300;
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
		
		if(this.lightStatus == Status.OFF && lightStatus == Status.ON)
			powerUsage += 20;
		if(this.lightStatus == Status.ON && lightStatus == Status.OFF)
			powerUsage -= 20;
		this.lightStatus = lightStatus;
	}

	public Status getHeaterStatus() {
		return conditioningUnitStatus;
	}

	public void setHeaterStatus(Status conditioningUnitStatus) {
		if(conditioningUnitStatus == Status.HEATING_MODE && this.conditioningUnitStatus == Status.COOLING_MODE) {
			powerUsage -= 300;
			powerUsage += 200;
		}
		if(conditioningUnitStatus == Status.COOLING_MODE && this.conditioningUnitStatus == Status.HEATING_MODE) {
			powerUsage -= 200;
			powerUsage += 300;
		}
		if(conditioningUnitStatus == Status.HEATING_MODE && this.conditioningUnitStatus == Status.MANUALLY_OFF)
			powerUsage += 200;
		if(conditioningUnitStatus == Status.COOLING_MODE && this.conditioningUnitStatus == Status.MANUALLY_OFF)
			powerUsage += 300;
		if(conditioningUnitStatus == Status.MANUALLY_OFF && this.conditioningUnitStatus == Status.COOLING_MODE)
			powerUsage -= 300;
		if(conditioningUnitStatus == Status.MANUALLY_OFF && this.conditioningUnitStatus == Status.HEATING_MODE)
			powerUsage -= 200;
		this.conditioningUnitStatus = conditioningUnitStatus;
	}
	
	public int getPowerUsage() {
		return powerUsage;
	}

	public String toString() {
		return "Room " + id + "  |  Temperature: " + df.format(temperature) + "°C  |  Target Temperature: " + df.format(targetTemp)
				+ "°C  |  Lights: " + lightStatus + "  |  Conditioning Unit: " + conditioningUnitStatus + "  |  Power Usage: " + powerUsage + " W";
	}
}
