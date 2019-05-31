package client;

import java.time.LocalTime;
import java.util.Scanner;

import cleaner.CleanManager;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import events.ClientEvents;

public class RaidBotClient {

	public static void main(String[] args) {
		//build client
		final DiscordClient client = new DiscordClientBuilder(args[0]).build();
		ClientEvents events = new ClientEvents(client);
		CleanManager cleaner = new CleanManager(client);
		//CleanManager cleaner = null;
		
		client.getEventDispatcher().on(ReadyEvent.class)
			.subscribe(e -> System.out.println(LocalTime.now() + " Logged in"));
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
					case "help":
						System.out.println("Available commands:\n");
						System.out.println("clean now\tforces a channel clean\n");
						System.out.println("cleans\t\tprint cleaning times for all channels\n");
						System.out.println("clear raids\tclears all (active) raids from bots memory\n");
						System.out.println("exit\t\tlogs the client out");
						break;
					case "clean now":
						cleaner.cleanNow();
						break;
					case "cleans":
						cleaner.printCleaningTimes();
						break;
					case "clear raids":
						ClientEvents.clearRaids();
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
