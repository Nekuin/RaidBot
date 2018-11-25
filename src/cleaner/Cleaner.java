package cleaner;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;

public class Cleaner {
	
	private IDiscordClient cli;
	private List<ChannelCleaner> cleanTasks;
	private Long[] channelsToClean = {
			//pogo espoon keskus testi kanava
			497132570924810261L,
			
			//pogo espoon keskus kannuja:
			//5-raidit
			497128671237373959L,
			//4-raidit
			497128696000544768L,
			//3-raidit
			497128710097600512L,
			//pienet raidit
			483648554288807946L,
			//havainnot
			497129234670813187L,
			
	};
	
	public Cleaner(IDiscordClient client) {
		this.cli = client;
		this.cleanTasks = new ArrayList<>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				//wait for client to log in
				while(!cli.isReady()) Thread.yield();
				//start a cleaner for each marked channel
				for(Long id : channelsToClean) {
					IChannel ch = cli.getChannelByID(id);
					ChannelCleaner cleaner = new ChannelCleaner(ch);
					cleanTasks.add(cleaner);
				}
			}
		}).start();
	}
	
	public void printCleaningTimes() {
		for(ChannelCleaner cl : cleanTasks) {
			cl.printCleaningTime();
		}
	}
	
}
