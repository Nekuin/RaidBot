package client;

import java.util.Scanner;

import cleaner.CleanManager;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import events.ClientEvents;

public class RaidBotClient {

	public static void main(String[] args) {
		//build client
		final DiscordClient client = new DiscordClientBuilder(args[0]).build();
		ClientEvents events = new ClientEvents(client);
		CleanManager cleaner = new CleanManager(client);
		
		System.out.println("logged in");
		startCommandThread(client, events, cleaner);
		
		
		client.login().block();
	}
	
	public static void startCommandThread(DiscordClient client, ClientEvents events, CleanManager cleaner) {
		//command line controls
		new Thread(() -> {
			Scanner scanner = new Scanner(System.in);
			String command = "";
			while(true) {
				command = scanner.nextLine();
				switch(command) {
				
					case "clean now":
						cleaner.cleanNow();
						break;
					case "cleans":
						cleaner.printCleaningTimes();
						break;
					case "clear raids":
						events.clearRaids();
						break;
					case "exit":
						client.logout();
						scanner.close();
						System.out.println("logged out");
						System.exit(0);
						break;
				}
			}
			
		}).start();
	}
}
