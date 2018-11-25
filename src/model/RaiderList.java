package model;

import java.util.ArrayList;
import java.util.List;

import client.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class RaiderList {
	
	private List<String> raiders;
	private String raidLocation;
	private String raidBoss;
	private String raidTime;
	private long timestamp;
	
	public RaiderList() {
		this.raiders = new ArrayList<>();
	}
	
	/**
	 * Sets a location for the raid, used in the string representation of the raid
	 * @param loc
	 */
	public void setRaidLocation(String loc) {
		this.raidLocation = loc.toLowerCase().trim();
	}
	
	/**
	 * returns the raid location
	 * @return
	 */
	public String getRaidLocation() {
		return this.raidLocation;
	}
	
	/**
	 * set a boss name for the raid
	 * @param boss
	 */
	public void setRaidBoss(String boss) {
		this.raidBoss = boss;
	}
	
	/**
	 * get raid boss name
	 * @return
	 */
	public String getRaidBoss() {
		return this.raidBoss;
	}
	
	/**
	 * sets the beginning time of the raid
	 * @param time
	 */
	public void setRaidTime(String time) {
		this.raidTime = time;
	}
	
	/**
	 * get the beginning time of the raid
	 * @return
	 */
	public String getRaidTime() {
		return this.raidTime;
	}
	
	/**
	 * add a name to the raider list
	 * @param name
	 */
	public void addRaider(String name) {
		this.raiders.add(name);
	}
	
	/**
	 * Remove a raider from the raider list
	 * @param name
	 */
	public void removeRaider(String name) {
		if(this.raiders.contains(name)) {
			this.raiders.remove(name);
		} else {
			System.out.println("name " + name + " was not found on " + getRaidLocation() + " raiders list");
		}
	}
	
	/**
	 * Remove a raider from index i from the raider list
	 * @param i - index to be removed
	 */
	public void removeRaiderByIndex(int i) {
		//list indexes are offset by +1
		i--;
		if(i < this.raiders.size()) {
			this.raiders.remove(i);
		} else {
			System.out.println("Index " + i + " was too big for " + getRaidLocation() + " raiders list");
		}
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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
		if(this.getRaidLocation() != null) {
			str += this.getRaidLocation() + " ";
		}
		if(this.getRaidTime() != null) {
			str += this.getRaidTime();
		}
		//there is always one raider, the one who made the raid, so no need for null check
		str += "\n" + this.getRaiders();
		return str;
	}
	
	public EmbedObject getEmbedObject() {
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(255, 0, 0);
		builder.appendField("Aika:", this.getRaidTime(), true);
		builder.appendField("Boss:", this.getRaidBoss(), true);
		builder.appendField("Paikka:", this.getRaidLocation(), false);
		builder.appendField("Ilmoittautuneet:", this.getRaiders(), false);
		String ohjeet = "Kysy apua: " + BotUtils.BOT_PREFIX + "help";
		builder.withFooterText(ohjeet);
		builder.withTimestamp(this.getTimestamp());

		return builder.build();
	}
}
