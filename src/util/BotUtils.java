package util;

import discord4j.core.object.entity.MessageChannel;

public class BotUtils {
	
	public static String BOT_PREFIX = "!";
	
	public static void sendMessage(MessageChannel channel, String message) {
		channel.createMessage(message).block();
	}
	
	
}
