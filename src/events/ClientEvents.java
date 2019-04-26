package events;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import model.Raid;
import model.RaidFactory;
import reactor.core.publisher.Mono;

/**
 * Handle user issued commands
 * @author Nekuin
 *
 */
public class ClientEvents {
	
	private List<Snowflake> channelList;
	private List<Raid> raidList;
	private Map<Snowflake, List<ReactionEmoji>> templateReactions;
	
	public ClientEvents(DiscordClient client) {
		raidList = new ArrayList<>();
		templateReactions = new HashMap<>();
		setupRaidChannels();
		setupTemplateReactions();
		subscribeToEvents(client);
	}
	
	private void subscribeToEvents(DiscordClient client) {
		// listen for raid messages
		client.getEventDispatcher().on(MessageCreateEvent.class)
				.map(MessageCreateEvent::getMessage)
				.filter(msg -> channelList.contains(msg.getChannelId()))
				.filter(msg -> msg.getContent()
						.map(content -> content.startsWith(BotUtils.BOT_PREFIX + "raid")).orElse(false))
				.subscribe(this::onRaidMessage);
		
		// listen for ReactionAdd events
		client.getEventDispatcher().on(ReactionAddEvent.class)
				.filter(event -> channelList.contains(event.getChannelId()))
				.filterWhen(event -> event.getUser().map(user -> !user.isBot()))
				.filter(event -> templateReactions.get(event.getGuildId().get()).contains(event.getEmoji()))
				.subscribe(this::onReactionAdd);
		
		// listen for ReactionRemove events
		client.getEventDispatcher().on(ReactionRemoveEvent.class)
			.filter(event -> channelList.contains(event.getChannelId()))
			.filterWhen(event -> event.getUser().map(user -> !user.isBot()))
			.filter(event -> templateReactions.get(event.getGuildId().get()).contains(event.getEmoji()))
			.subscribe(this::onReactionRemove); 
		
		// listen for help messages
		client.getEventDispatcher().on(MessageCreateEvent.class)
				.map(MessageCreateEvent::getMessage)
				.filter(msg -> msg.getContent()
						.map(content -> content.startsWith(BotUtils.BOT_PREFIX + "help")).orElse(false))
				.subscribe(this::onHelpMessage);
		
		// listen for time edit message
		client.getEventDispatcher().on(MessageCreateEvent.class)
			.map(MessageCreateEvent::getMessage)
			.filter(msg -> channelList.contains(msg.getChannelId()))
			.filter(msg -> msg.getContent()
					.map(content -> content.startsWith(BotUtils.BOT_PREFIX + "aika")).orElse(false))
			.subscribe(this::onTimeEditMessage);
		
		//listen for boss edit message
		client.getEventDispatcher().on(MessageCreateEvent.class)
			.map(MessageCreateEvent::getMessage)
			.filter(msg -> channelList.contains(msg.getChannelId()))
			.filter(msg -> msg.getContent()
					.map(content -> content.startsWith(BotUtils.BOT_PREFIX + "boss")).orElse(false))
			.subscribe(this::onBossEditMessage);

	}
	

	
	
	/**
	 * Creates a raid object when a user issues the !raid command
	 * Sends an embed object message to the same same channel
	 * Adds template reactions to the message
	 * @param msg
	 */
	private void onRaidMessage(Message msg) {
		//create Raid object
		Raid raid = RaidFactory.createRaid(msg);
		if(raid != null) {
			Mono.just(raid)
				.map(Raid::getChannel)
				.subscribe(ch -> {
					//set Message for raid object and send the EmbedObject to the channel
					raid.setMessage(ch.createEmbed(raid.getEmbedObject()
							.andThen(spec -> {}))
							.block());
					//add template reactions
					addTemplateReactions(raid);
					//remove duplicates if exists
					findAndRemoveDuplicates(raid);
					//add raid to raidList
					raidList.add(raid);
				});
		} else {
			sendRaidHelpMessage(msg);
		}
		//remove users message
		msg.delete().subscribe();
	} 
	
	/**
	 * Adds event author to a raid
	 * @param event
	 */
	private void onReactionAdd(ReactionAddEvent event) {
		Raid raid = findRaidById(event.getMessageId().asLong());
		if(raid != null) {
			//find out if the raider uses a nickname or not
			String name = Mono.just(event).flatMap(ReactionAddEvent::getGuild)
					.map(g -> getRaiderName(g.getMemberById(event.getUserId()).block())).block();

			//figure out how many times to add raider
			String emojiName = event.getEmoji().asCustomEmoji().get().getName();
			if(emojiName.equals("1_")) {
				raid.addRaider(name);
			} else if(emojiName.equals("2_")) {
				raid.addRaider(name, 2);
			} else if(emojiName.equals("3_")) {
				raid.addRaider(name, 3);
			}
			
			//edit message
			raid.getMessage().edit(spec -> spec.setEmbed(raid.getEmbedObject())).subscribe();
			
		}
	}
	
	/**
	 * Removes event author from a raid
	 * @param event
	 */
	private void onReactionRemove(ReactionRemoveEvent event) {
		Raid raid = findRaidById(event.getMessageId().asLong());
		if(raid != null) {
			//find out if the raider uses a nickname or not
			String name = Mono.just(event).flatMap(ReactionRemoveEvent::getGuild)
					.map(g -> getRaiderName(g.getMemberById(event.getUserId()).block())).block();
			
			//figure out how many times to remove the raider
			String emojiName = event.getEmoji().asCustomEmoji().get().getName();
			if(emojiName.equals("1_")) {
				raid.removeRaider(name);
			} else if(emojiName.equals("2_")) {
				raid.removeRaider(name, 2);
			} else if(emojiName.equals("3_")) {
				raid.removeRaider(name, 3);
			}
			
			//edit message
			raid.getMessage().edit(spec -> spec.setEmbed(raid.getEmbedObject())).subscribe();
		}
	}
	
	/**
	 * Fix or change the starting time for a raid
	 * @param message
	 */
	private void onTimeEditMessage(Message message) {
		String[] split = message.getContent().get().split(" ");
		String aika = split[1];
		String loc = "";
		//location SHOULD start from index 2, command is !aika TIME LOCATION (LOCATION)
		for(int i = 2; i < split.length; i++) {
			loc += split[i] + " ";
		}
		loc = loc.trim();
		Raid raid = findRaidByLocation(loc);
		if(raid != null) {
			//change time
			raid.getRaiderList().setRaidTime(aika);
			System.out.println(LocalTime.now() + " [FIX TIME] - Found location " + loc + " fixing time...");
			//edit message
			raid.getMessage().edit(spec -> spec.setEmbed(raid.getEmbedObject())).subscribe();
		}
		//remove users message
		message.delete().subscribe();
	}
	
	/**
	 * Fix or change the boss for a raid
	 * @param message
	 */
	private void onBossEditMessage(Message message) {
		String[] split = message.getContent().get().split(" ");
		String boss = split[1];
		String loc = "";
		//location SHOULD start from index 2, command is !boss TIME LOCATION (LOCATION)
		for(int i = 2; i < split.length; i++) {
			loc += split[i] + " ";
		}
		loc = loc.trim();
		Raid raid = findRaidByLocation(loc);
		if(raid != null) {
			//change boss
			raid.getRaiderList().setRaidBoss(boss);
			System.out.println(LocalTime.now() + " [FIX BOSS] - Found location " + loc + " fixing boss...");
			//edit message
			raid.getMessage().edit(spec -> spec.setEmbed(raid.getEmbedObject())).subscribe();
		}
		//remove users message
		message.delete().subscribe();
	}
	
	/**
	 * Sends a help message to the message author
	 * @param message
	 */
	private void onHelpMessage(Message message) {
		String helpMessage = "Create a raid:\n"
				+ "```" + BotUtils.BOT_PREFIX + "raid STARTINGTIME BOSS LOCATION\n"
				+ "i.e. " + BotUtils.BOT_PREFIX + "raid 12.00 Mewtwo Suvelan Tammi```\n"
				+ "Join a raid:\n"
				+ "```Click on reactions +1, +2 or +3, the number indicates how many devices(or persons) you sign up for the raid```\n"
				+ "Fix/change the time on a raid\n"
				+ "```" + BotUtils.BOT_PREFIX + "aika TIME LOCATION\n"
				+ "i.e. " + BotUtils.BOT_PREFIX + "aika 15:00 Suvelan Tammi```\n"
				+ "Change boss's name on a raid:\n"
				+ "```"+ BotUtils.BOT_PREFIX + "boss BOSS LOCATION\n"
				+ "i.e. " + BotUtils.BOT_PREFIX + "boss mew Suvelan Tammi```\n"
				+ "Can't see the bot's message? Make sure link preview is enabled.\n"
				+ "Questions or feedback? Contact Nekuin#3936 on discord.\n\n"
				
				+ "Ilmoita uusi raidi: \n"
				+ "```" + BotUtils.BOT_PREFIX + "raid ALOITUSAIKA BOSSI PAIKKA\n"
				+ "ESIM: " + BotUtils.BOT_PREFIX + "raid 12.00 Mewtwo Suvelan Tammi```\n"
				+ "Ilmoita itsesi raidiin: \n"
				+ "```Klikkaa reaktioita +1 +2 tai +3, numero kertoo kuinka monta ilmoitat```\n"
				+ "Korjaa aika raidiin: \n"
				+ "```" + BotUtils.BOT_PREFIX + "aika AIKA PAIKKA\n"
				+ "ESIM: " + BotUtils.BOT_PREFIX + "aika 15:00 Suvelan Tammi```\n"
				+ "Vaihda bossin nimi raidiin: \n"
				+ "```" + BotUtils.BOT_PREFIX + "boss BOSS PAIKKA\n"
				+ "ESIM: " + BotUtils.BOT_PREFIX + "boss mew Suvelan Tammi```\n"
				+ "Etkö näe botin viestiä? Varmista että linkkien esikatselu on päällä.\n"
				+ "Kysymykset ja palautteet voi laittaa Discordissa Nekuin#3936.";
		//send help message
		message.getAuthor().get().getPrivateChannel().block()
			.createMessage(spec -> spec.setContent(helpMessage)).subscribe();
		message.delete().subscribe();
	}
	
	/**
	 * Sends a help message with instructions on how to create a raid to the message author
	 * @param msg
	 */
	private void sendRaidHelpMessage(Message msg) {
		System.out.println(LocalTime.now() + " [CREATE RAID] - Problem creating a raid, sending instructions");
		String helpMsg = "Jotain puuttui " + BotUtils.BOT_PREFIX + "raid komennostasi...\n"
				+ "```" + msg.getContent().get() + "```"
				+ "Koita näin: \n"
				+ "```" + BotUtils.BOT_PREFIX + "raid AIKA BOSS PAIKKA\n"
						+ BotUtils.BOT_PREFIX + "raid 12:00 mew suvelan tammi``` \n"
				+ "Something was missing from your " + BotUtils.BOT_PREFIX + "raid command... \n"
				+ "Try this: \n"
				+ "```" + BotUtils.BOT_PREFIX + "raid TIME BOSS LOCATION```";
		//send help message
		msg.getAuthor().get().getPrivateChannel().block()
			.createMessage(spec -> spec.setContent(helpMsg)).subscribe();
	}
	
	/**
	 * Add template +1 +2 +3 reactions to the raid message
	 * @param raid
	 */
	private void addTemplateReactions(Raid raid) {
		Snowflake guildId = raid.getMessage().getGuild().block().getId();
		templateReactions.get(guildId).forEach(e -> {
			//add reactions to msg
			raid.getMessage().addReaction(e).block();
			//block() instead of subscribe() seems to help getting the reactions in order...
			//at least on PC version, Android seems to work quite differently
		});
	}
	
	/**
	 * Find raid from the raidList by message ID
	 * @param id
	 * @return
	 */
	private Raid findRaidById(long id) {
		for(Raid raid : raidList) {
			if(raid.getMessage().getId().asLong() == id) {
				return raid;
			}
		}
		return null;
	}
	
	/**
	 * Find a Raid object by location
	 * @param location
	 * @return
	 */
	private Raid findRaidByLocation(String location) {
		for(Raid raid : raidList) {
			if(raid.getLocation().equals(location)) {
				return raid;
			}
		}
		return null;
	}
	
	private void findAndRemoveDuplicates(Raid raid) {
		boolean found = false;
		Raid duplicate = new Raid();
		for(Raid r : raidList) {
			if(r.getLocation().equals(raid.getLocation())) {
				//duplicate raid location found, check for channel also
				if(r.getChannel().equals(raid.getChannel())) {
					duplicate = r;
					found = true;
				}
			}
		}
		if(found) {
			duplicate.getMessage().delete().subscribe();
			raidList.remove(duplicate);
		}
	}
	
	/**
	 * Get a users name, returns Nickname by default if a user has one
	 * @param member
	 * @return
	 */
	private String getRaiderName(Member member) {
		return (member.getNickname().isPresent()) ? member.getNickname().get() : member.getUsername();
	}
	

	/**
	 * Add template reactions to a HashMap with guild ID as the key
	 */
	private void setupTemplateReactions() {
		//pogo espoon keskus
		List<ReactionEmoji> espoonKeskus = new ArrayList<>();
		espoonKeskus.add(ReactionEmoji.of(496747453132046357L, "1_", false));
		espoonKeskus.add(ReactionEmoji.of(496747481196003331L, "2_", false));
		espoonKeskus.add(ReactionEmoji.of(496747490486255635L, "3_", false));
		
		//testi servu
		List<ReactionEmoji> testServu = new ArrayList<>();
		testServu.add(ReactionEmoji.of(503269083953758265L, "1_", false));
		testServu.add(ReactionEmoji.of(503269083731460107L, "2_", false));
		testServu.add(ReactionEmoji.of(503269084075393024L, "3_", false));
		
		templateReactions.put(Snowflake.of(483647882587537408L), espoonKeskus);
		templateReactions.put(Snowflake.of(498168048717398016L), testServu);
	}
	
	/**
	 * Add channel IDs to a list where the bot reacts to the messages
	 */
	private void setupRaidChannels() {
		channelList = new ArrayList<>();
		//test servu general
		channelList.add(Snowflake.of(498168048717398019L));
		
		// espoon keskus pogo kannut:
		//testatkaa botin toimintaa kannu:
		channelList.add(Snowflake.of(497132570924810261L));
		//5raids
		channelList.add(Snowflake.of(497128671237373959L));
		//4raids
		channelList.add(Snowflake.of(497128696000544768L));
		//1-2-3 raidit
		channelList.add(Snowflake.of(497128710097600512L));
		//ex raids
		channelList.add(Snowflake.of(483648699499806720L));
		//community day ch
		channelList.add(Snowflake.of(487710843639824404L));
	}
	
	public void clearRaids() {
		System.out.println(LocalTime.now() + " Clearing raid list..");
		raidList.clear();
	}
}
