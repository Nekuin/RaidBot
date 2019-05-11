package cleaner;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;

public class ChannelCleaner {
	
	private TextChannel channel;
	private Calendar cal;
	
	
	public ChannelCleaner(TextChannel channel) {
		this.channel = channel;
		cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, day+1);
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 01);
		cal.set(Calendar.SECOND, 00);
		
		System.out.println(LocalTime.now() + " Cleaning job started for channel: " 
				+ channel.getGuild().block().getName() + "/" + channel.getName() + "\n"
				+ "First clean scheduled at " + this.cal.getTime()
				);
		
		Timer timer = new Timer();
		timer.schedule(new CleanTask(timer), cal.getTime());
	}
	
	private void clean() {
		System.out.println(LocalTime.now() + " Cleaning channel " + channel.getGuild().block().getName() + "/" + channel.getName());
		//max 2 weeks old messages
		Instant i = Instant.ofEpochSecond(Instant.now().getEpochSecond()-1209600);
		List<Snowflake> messages = channel.getMessagesBefore(Snowflake.of(Instant.now())).take(100)
			.filter(msg -> msg.getTimestamp().isAfter(i))
			.filter(msg -> !msg.isPinned())
			.map(Message::getId)
			.collect(Collectors.toList())
			.block();
		
		while(messages.size() > 0) {
			//bulk delete messages
			channel.bulkDelete(Flux.fromIterable(messages)).subscribe();
			messages = channel.getMessagesBefore(Snowflake.of(Instant.now())).take(100)
					.filter(msg -> msg.getTimestamp().isAfter(i))
					.filter(msg -> !msg.isPinned())
					.map(Message::getId)
					.collect(Collectors.toList())
					.block();
					
		}
		
		System.out.println("finished cleaning in channel: " + channel.getGuild().block().getName() + "/" + channel.getName());
	}
	
	public void printCleaningTime() {
		System.out.println(channel.getGuild().block().getName() + "/" + channel.getName() + " cleaning time is: " + cal.getTime().toString());
	}
	
	public void cleanNow() {
		clean();
	}
	
	class CleanTask extends TimerTask {
		private Timer timer;
		
		public CleanTask(Timer timer) {
			this.timer = timer;
		}
		
		@Override
		public void run() {
			clean();
			timer.cancel();
			int day = cal.get(Calendar.DAY_OF_MONTH);
			cal.set(Calendar.DAY_OF_MONTH, day+1);
			cal.set(Calendar.HOUR_OF_DAY, 00);
			cal.set(Calendar.MINUTE, 01);
			cal.set(Calendar.SECOND, 00);
			Timer newt = new Timer();
			newt.schedule(new CleanTask(newt), cal.getTime());
		}
	}
}
