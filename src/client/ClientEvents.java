package client;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import model.*;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.util.RequestBuffer;

public class ClientEvents {
	
	private List<Raid> raids;
	private Long[] raidChannels = {
			//espoon keskus kannut:
			//testatkaa botin toimintaa kannu:
			497132570924810261L, 
			//5raids:
			497128671237373959L, 
			//4raids:
			497128696000544768L, 
			//3raids:
			497128710097600512L, 
			//ex raids:
			483648699499806720L, 
			//raid ilm pienet:
			483648554288807946L,
			//community day ch
			487710843639824404L,
			
			
			//testi servu:
			//general:
			498168048717398019L
			
	};
	
	public ClientEvents() {
		this.raids = new ArrayList<>();
	}
	//get RaiderList based on say RaidLocation
	//get messageID by RaiderList
	
	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContent().toLowerCase();
		IChannel eventch = event.getChannel();
		
		boolean correctChannel = false;
		for(Long chid : raidChannels) {
			if(eventch.getLongID() == chid) {
				correctChannel = true;
			}
		}
		
		//only take raid commands in the raid channels
		if(correctChannel) {
			if(message.startsWith(BotUtils.BOT_PREFIX + "raid")) {
				//this.postRaid(event);
				this.createRaid(event);
			}
			
			if(message.startsWith(BotUtils.BOT_PREFIX + "aika")) {
				this.fixTime(event);
			}
			
			if(message.startsWith(BotUtils.BOT_PREFIX + "boss")) {
				this.fixBoss(event);
			}
		}
		if(message.startsWith(BotUtils.BOT_PREFIX + "help")) {
			IPrivateChannel p = event.getMessage().getAuthor().getOrCreatePMChannel();
			RequestBuffer.request(() -> {
				p.sendMessage(
						"Create a raid:\n"
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
						+ "Kysymykset ja palautteet voi laittaa Discordissa Nekuin#3936.");
				event.getMessage().delete();
			});
		}
		
	}
	
	
	@EventSubscriber
	public void onReactionAdd(ReactionAddEvent event) {
		boolean correctChannel = false;
		boolean hasNickName = false;
		
		//check channel
		for(Long chid : raidChannels) {
			if(event.getChannel().getLongID() == chid) {
				correctChannel = true;
			}
		}
		
		if(!correctChannel) return;
		
		//check if user has a nickname
		if(event.getUser().getNicknameForGuild(event.getGuild()) != null) {
			System.out.println(LocalTime.now() + " [REACTION ADD] - nickname found: " + event.getUser().getNicknameForGuild(event.getGuild()));
			hasNickName = true;
		}
		
		//ignore the bot
		if(event.getUser().getName().equals(BotUtils.BOT_NAME)) {
			//System.out.println(LocalTime.now() + " usr was: " + event.getUser().getName() + " ignoring");
			return;
		}

		String emojiName = event.getReaction().getEmoji().getName();
		//find raid
		//Raid raid = this.findRaidFromEmbed(event.getMessage());
		Raid raid = this.findRaidWithID(event.getMessageID());
		if(raid != null) {
			boolean emojiOne = (emojiName.equals("1_")) ? true : false;
			boolean emojiTwo = (emojiName.equals("2_")) ? true : false;
			boolean emojiThree = (emojiName.equals("3_")) ? true : false;
			
			String name = hasNickName ? event.getUser().getNicknameForGuild(event.getGuild()) : event.getUser().getName();
			if(emojiOne) {
				raid.addRaider(name);
			} else if(emojiTwo) {
				for(int i = 0; i < 2; i++) {
					raid.addRaider(name);
				}
			} else if(emojiThree) {
				for(int i = 0; i < 3; i++) {
					raid.addRaider(name);
				}
			}
			//edit changes to the message
			RequestBuffer.request(() -> {
				raid.getIMessage().edit(raid.getEmbedObject());
			});
			
		} else {
			System.out.println(LocalTime.now() + " [REACTION ADD] - Raid was null " 
					+ event.getGuild().getName() + "/" + event.getChannel().getName());
		}
		
	}
	
	@EventSubscriber
	public void onReactionRemove(ReactionRemoveEvent event) {
		boolean correctChannel = false;
		boolean hasNickName = false;
		
		for(Long chid : raidChannels) {
			if(event.getChannel().getLongID() == chid) {
				correctChannel = true;
			}
		}
		
		if(!correctChannel) return;
		
		if(event.getUser().getNicknameForGuild(event.getGuild()) != null) {
			System.out.println(LocalTime.now() + " [REACTION REM] - nickname found: " + event.getUser().getNicknameForGuild(event.getGuild()));
			hasNickName = true;
		}
		
		
		//ignore the bot
		if(event.getUser().getName().equals(BotUtils.BOT_NAME)) {
			//System.out.println(LocalTime.now() + " usr was: " + event.getUser().getName() + " ignoring");
			return;
		}
		
		String emojiName = event.getReaction().getEmoji().getName();
		//find raid
		//Raid raid = this.findRaidFromEmbed(event.getMessage());
		Raid raid = this.findRaidWithID(event.getMessageID());
		if(raid != null) {
			boolean emojiOne = (emojiName.equals("1_")) ? true : false;
			boolean emojiTwo = (emojiName.equals("2_")) ? true : false;
			boolean emojiThree = (emojiName.equals("3_")) ? true : false;
			
			String name = hasNickName ? event.getUser().getNicknameForGuild(event.getGuild()) : event.getUser().getName();
			if(emojiOne) {
				raid.removeRaider(name);
			} else if(emojiTwo) {
				for(int i = 0; i < 2; i++) {
					raid.removeRaider(name);
				}
			} else if(emojiThree) {
				for(int i = 0; i < 3; i++) {
					raid.removeRaider(name);
				}
			}
			//edit changes to the message
			RequestBuffer.request(() -> {
				raid.getIMessage().edit(raid.getEmbedObject());
			});
		} else {
			System.out.println(LocalTime.now() + " [REACTION REM] - Raid was null " 
					+ event.getGuild().getName() + "/" + event.getChannel().getName());
		}
		
	}
	
	private Raid findRaidWithID(long msgID) {
		for(Raid raid : raids) {
			if(raid.getIMessage().getLongID() == msgID) {
				return raid;
			}
		}
		return null;
	}
	
	
	/**
	 * Search for duplicate raids and find the bot's own message to get the ID
	 * @param raid Raid object
	 */
	private void findDuplicates(Raid raid) {
		boolean found = false;
		Raid duplicate = new Raid();
		for(Raid r : this.raids) {
			if(r.getRaidLocation().equals(raid.getRaidLocation())) {
				//duplicate raid location found, check channel
				if(r.getIChannel().equals(raid.getIChannel())) {
					duplicate = r;
					found = true;
				}
			}
		}
		if(found) {
			duplicate.getIMessage().delete();
			this.raids.remove(duplicate);
		}
	}
	
	private void createRaid(MessageReceivedEvent event) {
		Raid raid = new RaidFactory().createRaid(event.getMessage().getContent());
		if(raid != null) {
			raid.setIChannel(event.getChannel());
			RequestBuffer.request(() -> {
				IMessage msg = raid.getIChannel().sendMessage(raid.getEmbedObject());
				raid.setIMessage(msg);
				this.findDuplicates(raid);
				this.raids.add(raid);
				System.out.println(LocalTime.now() + " [CREATE RAID] - Raid successfully created; location: " + raid.getRaidLocation() + ", channel: "
						+ raid.getIChannel().getName() + ", guild: " + event.getGuild().getName());
				this.addTemplateReactions(raid);
			});
		} else {
			System.out.println("[CREATE RAID] - Problem creating a raid, sending instructions");
			IPrivateChannel p = event.getMessage().getAuthor().getOrCreatePMChannel();
			p.sendMessage("Jotain puuttui " + BotUtils.BOT_PREFIX + "raid komennostasi...\n"
					+ "```" + event.getMessage().getContent() + "```"
					+ "Koita näin: \n"
					+ "```" + BotUtils.BOT_PREFIX + "raid AIKA BOSS PAIKKA\n"
							+ BotUtils.BOT_PREFIX + "raid 12:00 mew suvelan tammi``` \n"
					+ "Something was missing from your " + BotUtils.BOT_PREFIX + "raid command... \n"
					+ "Try this: \n"
					+ "```" + BotUtils.BOT_PREFIX + "raid TIME BOSS LOCATION```");
		}
		RequestBuffer.request(() -> {
			event.getMessage().delete();
		});
	}
	
	private void addTemplateReactions(Raid raid) {
		System.out.println(LocalTime.now() + " adding template reactions..");
		//add template reactions
		//type \:emojiName: into discord chat to get custom emoji name and ID
		ReactionEmoji one;
		ReactionEmoji two;
		ReactionEmoji three;
		long guildID = raid.getIChannel().getGuild().getLongID();
		
		//PoGo Espoon keskus
		if(guildID == 483647882587537408L) {
			one = ReactionEmoji.of("1_", 496747453132046357L);
			two = ReactionEmoji.of("2_", 496747481196003331L);
			three = ReactionEmoji.of("3_", 496747490486255635L);
		} else if(guildID == 498168048717398016L) {
			//huutis servu (testi servu)
			one = ReactionEmoji.of("1_", 503269083953758265L);
			two = ReactionEmoji.of("2_", 503269083731460107L);
			three = ReactionEmoji.of("3_", 503269084075393024L);
		} else {
			one = null;
			two = null;
			three = null;
		}
		
		if(one != null || two != null || three != null) {
			new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						RequestBuffer.request(() -> {
							raid.getIMessage().addReaction(one);
						});
						Thread.sleep(500);
						RequestBuffer.request(() -> {
							raid.getIMessage().addReaction(two);
						});
						Thread.sleep(500);
						RequestBuffer.request(() -> {
							raid.getIMessage().addReaction(three);
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			System.out.println(LocalTime.now() + " some emoji was null"
					+ raid.getIChannel().getGuild().getName() + "/" + raid.getIChannel().getName());
		}
	}
	
	
	
	
	//fix or change time from a raid
	private void fixTime(MessageReceivedEvent event) {
		String[] split = event.getMessage().getContent().split(" ");
		String aika = split[1];
		String loc = "";
		for(int i = 2; i < split.length; i++) {
			loc += split[i] + " ";
		}
		loc = loc.trim();
		for(Raid raid : raids) {
			if(raid.getRaidLocation().equals(loc)) {
				if(raid.getIChannel().equals(event.getChannel())) {
					RaiderList rl = raid.getRaiderList();
					rl.setRaidTime(aika);
					System.out.println(LocalTime.now() + " [FIX TIME] - Found location " + loc + " from channel: " 
					+ raid.getIChannel().getName() + " fixing time...");
					RequestBuffer.request(() -> {
						raid.getIMessage().edit(rl.getEmbedObject());
					});
				}
			}
		}
		RequestBuffer.request(() -> {
			event.getMessage().delete();
		});
	}
	
	//fix or change the boss from a raid
	private void fixBoss(MessageReceivedEvent event) {
		String[] split = event.getMessage().getContent().split(" ");
		String boss = split[1];
		String loc = "";
		for(int i = 2; i < split.length; i++) {
			loc += split[i] + " ";
		}
		loc = loc.trim();
		for(Raid raid : raids) {
			if(raid.getRaidLocation().equals(loc)) {
				if(raid.getIChannel().equals(event.getChannel())) {
					RaiderList rl = raid.getRaiderList();
					rl.setRaidBoss(boss);
					System.out.println(LocalTime.now() + " [FIX BOSS] - Found location " + loc + " from channel: "
							+ raid.getIChannel().getName() + " fixing boss...");
					RequestBuffer.request(() -> {
						raid.getIMessage().edit(rl.getEmbedObject());
					});
				}
			}
		}
		RequestBuffer.request(() -> {
			event.getMessage().delete();
		});
	}
	
	public void clearRaids() {
		System.out.println(LocalTime.now() + " Clearing raid list...");
		this.raids = new ArrayList<>();
	}
}
