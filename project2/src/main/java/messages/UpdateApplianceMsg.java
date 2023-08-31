package messages;

import java.io.Serializable;

public class UpdateApplianceMsg implements Serializable{
	
	String appliances;
	int usage;
	
	public UpdateApplianceMsg(String app, int usage) {
		this.appliances = app;
		this.usage = usage;
	}

	public String getAppliances() {
		return appliances;
	}

	public int getUsage() {
		return usage;
	}

}
