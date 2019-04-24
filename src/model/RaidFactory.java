package model;


public class RaidFactory {
	
	/**
	 * Creates a Raid object from a Discord message content
	 * @param message
	 * @return
	 */
	public static Raid createRaid(String message) {
		String[] split = message.split(" ");
		//lenght of 4 (or greater) == /raid TIME BOSS LOCATION (LOCATION)
		if(split.length >= 4) {
			RaiderList rl = new RaiderList();
			Raid raid = new Raid();
			rl.setRaidTime(split[1]);
			rl.setRaidBoss(split[2]);
			String raidLoc = "";
			for(int i = 3; i < split.length; i++) {
				raidLoc += split[i] + " ";
			}
			raidLoc = raidLoc.trim();
			rl.setRaidLocation(raidLoc);
			
			raid.setRaiderList(rl);
			rl.setTimestamp(System.currentTimeMillis());
			return raid;
		}
		return null;
	}
	
}
