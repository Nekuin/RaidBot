package client;

import java.util.Scanner;

import cleaner.Cleaner;
import sx.blah.discord.api.IDiscordClient;


public class BotClient{
	
	public static void main(String[] args) {
		
		ClientEvents clientEvents = new ClientEvents();
		//pass token as args
		IDiscordClient cli = BotUtils.getBuiltDiscordClient(args[0]);
		cli.getDispatcher().registerListener(clientEvents);
		cli.login();

		//start cleaning tasks
		Cleaner cleaner = new Cleaner(cli);
		
		
		Scanner sc = new Scanner(System.in);
		String msg = "";
		while(true) {
			msg = sc.nextLine();
			switch(msg) {
			case "e":
				sc.close();
				cli.logout();
				System.exit(0);
				break;
			case "c":
				clientEvents.clearRaids();
				break;
			case "cleans":
				cleaner.printCleaningTimes();
				break;
			default:
				System.out.println("not a valid command");
				break;
			}
		}
		
	}


}
