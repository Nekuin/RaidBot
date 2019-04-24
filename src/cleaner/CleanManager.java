package cleaner;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

public class CleanManager {

	private DiscordClient client;
	private List<ChannelCleaner> cleanTasks;
	private List<Snowflake> channelList;
	
	public CleanManager(DiscordClient client) {
		this.client = client;
		cleanTasks = new ArrayList<>();
		channelList = new ArrayList<>();
		setupChannels();
		client.getEventDispatcher().on(ReadyEvent.class)
			.subscribe(e -> start());
	}
	
	private void start() {
		System.out.println("[CleanManager] Starting..");
		new Thread(() -> {
			
			for(Snowflake chid : channelList) {
				TextChannel channel = (TextChannel)client.getChannelById(chid).block();
				
				cleanTasks.add(new ChannelCleaner(channel));
			}
		}).start();
	}
	
	public void printCleaningTimes() {
		cleanTasks.forEach(ChannelCleaner::printCleaningTime);
	}
	
	public void cleanNow() {
		cleanTasks.forEach(ChannelCleaner::cleanNow);
	}
	
	private void setupChannels() {
		//test server
		channelList.add(Snowflake.of(498168048717398019L));
		channelList.add(Snowflake.of(498171854742355968L));
		
		// espoon keskus pogo kannut:
		//testatkaa botin toimintaa kannu:
		channelList.add(Snowflake.of(497132570924810261L));
		//5raids
		channelList.add(Snowflake.of(497128671237373959L));
		//4raids
		channelList.add(Snowflake.of(497128696000544768L));
		//1-2-3 raidit
		channelList.add(Snowflake.of(497128710097600512L));
	}
}
