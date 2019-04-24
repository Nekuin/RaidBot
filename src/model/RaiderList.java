package model;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * Keeps track of Raiders and can construct a String and Embed representation of a Raid
 * @author Nekuin
 *
 */
public class RaiderList {

	private List<String> raiders;
	private String raidLocation;
	private String raidBoss;
	private String raidTime;
	private long timestamp;
	
	public RaiderList() {
		this.raiders = new ArrayList<>();
	}
	
	public void setRaidLocation(String location) {
		raidLocation = location.toLowerCase().trim();
	}
	
	public String getLocation() {
		return raidLocation;
	}
	
	public void setRaidBoss(String boss) {
		raidBoss = boss;
	}
	
	public String getRaidBoss() {
		return raidBoss;
	}
	
	public void setRaidTime(String time) {
		raidTime = time;
	}
	
	public String getRaidTime() {
		return raidTime;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void addRaider(String raider) {
		raiders.add(raider);
	}
	
	public void removeRaider(String raider) {
		raiders.remove(raider);
	}
	
	/**
	 * returns a string representation of signed up names on the list
	 * i.e.
	 * 1. Name
	 * 2. Name
	 * 3. Name
	 * 
	 * @return String
	 */
	public String getRaiders() {
		String str = "";
		int i = 1;
		for(String raider : raiders) {
			str += i + ". " + raider + "\n";
			i++;
		}
		if(str.equals("")) {
			str += "Ei ilmoittautuneita.";
		}
		return str;
	}
	
	/**
	 * returns a complete string representation of the raid
	 * i.e.
	 * Boss
	 * Location
	 * Time
	 * 1. Name
	 * 2. Name
	 * 3. Name
	 * @return
	 */
	public String getRaidString() {
		String str = "";
		if(this.getRaidBoss() != null) {
			str += this.getRaidBoss() + " ";
		}
		if(this.getLocation() != null) {
			str += this.getLocation() + " ";
		}
		if(this.getRaidTime() != null) {
			str += this.getRaidTime();
		}
		//there is always one raider, the one who made the raid, so no need for null check
		str += "\n" + this.getRaiders();
		return str;
	}
	
	public Consumer<EmbedCreateSpec> getEmbedObject = spec -> {
		spec.setColor(new Color(255, 0, 0));
		spec.addField("Aika:", getRaidTime(), true);
		spec.addField("Boss:", getRaidBoss(), true);
		spec.addField("Paikka:", getLocation(), false);
		spec.addField("Ilmoittautuneet:", getRaiders(), false);
		spec.setFooter("Kysy apua: !help", "");
		spec.setTimestamp(Instant.ofEpochMilli(timestamp));
		
	};
}
