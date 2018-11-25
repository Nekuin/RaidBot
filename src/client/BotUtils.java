package client;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {
	
	public static String BOT_PREFIX = "!";
	public static String BOT_NAME = "RaidBot";
	
	static IDiscordClient getBuiltDiscordClient(String token) {
		return new ClientBuilder()
				.withToken(token)
				.build();
	}
	
	static void sendMessage(IChannel channel, String message) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(message);
			} catch (DiscordException e) {
				e.printStackTrace();
			}
		});
	}
	
	static void sendEmbedMessage(IChannel channel, EmbedObject o) {
		RequestBuffer.request(() -> {
			channel.sendMessage(o);
		});
	}
	
}
