package objects;

import java.util.ArrayList;

public class Appliance {
	
	private String description;
	private Status isON;
	private int usage;

	public Appliance(String description, int usage) {
		this.description = description;
		this.isON = Status.OFF;
		this.usage = usage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Status getIsON() {
		return isON;
	}

	public void setIsON(Status isON) {
		this.isON = isON;
	}
	
	public int getUsage() {
		return usage;
	}
	
	public static String toString (ArrayList<Appliance> list) {
		String ret = "";
		for(Appliance a : list)
			ret += "| " + a.getDescription() + " is " + a.getIsON() + " ";
		return ret;
	}
}
