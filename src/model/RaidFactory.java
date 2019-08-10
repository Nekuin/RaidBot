package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import discord4j.core.object.entity.Message;

public class RaidFactory {
	
	/**
	 * Creates a Raid object from a Discord message content
	 * @param message
	 * @return
	 */
	public static Raid createRaid(Message message) {
		
		String[] split = message.getContent().get().split(" ");
		//lenght of 4 (or greater) == /raid TIME BOSS LOCATION (LOCATION)
		if(split.length >= 4) {
			RaiderList rl = new RaiderList();
			Raid raid = new Raid();
			String raidTime = split[1];
			if(raidTime.startsWith("+")) {
				String minutes = raidTime.substring(1, raidTime.length());
				try {
					Long mins = Long.parseLong(minutes);
					LocalTime newTime = LocalTime.now().plusMinutes(mins);
					String timestr = newTime.format(DateTimeFormatter.ofPattern("HH:mm"));
					raidTime = timestr;
				} catch(NumberFormatException e) {
					System.out.println(LocalTime.now() + " Error parsing time from: " + raidTime);
					LocalTime localErrorTime = LocalTime.now();
					String errorTime = localErrorTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "?";
					raidTime = errorTime;
				}
				
			}
			
			rl.setRaidTime(raidTime);
			rl.setRaidBoss(split[2]);
			String raidLoc = "";
			for(int i = 3; i < split.length; i++) {
				raidLoc += split[i] + " ";
			}
			raidLoc = raidLoc.trim();
			rl.setRaidLocation(raidLoc);
			
			raid.setRaiderList(rl);
			rl.setTimestamp(System.currentTimeMillis());
			raid.setChannel(message.getChannel().block());
			return raid;
		}
		return null;
	}
	
}
