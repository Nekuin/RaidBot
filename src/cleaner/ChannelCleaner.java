package cleaner;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.RequestBuffer;

public class ChannelCleaner {
	
	private IChannel channel;
	private Calendar cal;
	private MessageHistory hist;
	
	public ChannelCleaner(IChannel channel) {
		this.channel = channel;
		this.cal = Calendar.getInstance();
		int day = this.cal.get(Calendar.DAY_OF_MONTH);
		this.cal.set(Calendar.DAY_OF_MONTH, day+1);
		this.cal.set(Calendar.HOUR_OF_DAY, 00);
		this.cal.set(Calendar.MINUTE, 01);
		this.cal.set(Calendar.SECOND, 00);
		
		//announce starting a task, guild + channel name and time of the first clean
		System.out.println(LocalTime.now() + " Cleaning job started for channel: " 
				+ channel.getGuild().getName() + "/" + channel.getName() + "\n"
				+ "First clean scheduled at " + this.cal.getTime()
				);
		Timer timer = new Timer();
		timer.schedule(new CleanTask(timer), this.cal.getTime());
	}
	
	public void printCleaningTime() {
		System.out.println(this.channel.getGuild().getName() + "/" + this.channel.getName() + " cleaning time is: " + this.cal.getTime());
	}
	
	private void clean() {
		System.out.println(LocalTime.now() + " [CLEANER] - Starting clean on " + this.channel.getName());
		this.hist = this.channel.getMessageHistory(100);
		if(this.hist.size() == 0) {
			return;
		}
		
		RequestBuffer.request(() -> {
			try {
				//filter pinned messages
				List<IMessage> toDelete = this.hist.stream()
						.filter(msg -> !msg.isPinned())
						.collect(Collectors.toList());
				
				//bulkDelete messages
				List<IMessage> deleted = this.channel.bulkDelete(toDelete);
				//if deleted.size() > 0, there could be more to delete
				while(deleted.size() > 0) {
					System.out.println(LocalTime.now() + " - [CLEAN] > 0 msgs in " + this.channel.getName());
					this.hist = this.channel.getMessageHistory(100);
					toDelete = this.hist.stream()
							.filter(msg -> !msg.isPinned())
							.collect(Collectors.toList());
					deleted = this.channel.bulkDelete(toDelete);
				}
				System.out.println(LocalTime.now() + " - [CLEAN] " + this.channel.getName() + " stopping.");
				
			} catch (DiscordException e) {
				System.out.println(LocalTime.now() + " - [CLEAN] (Channel: " + this.channel.getName() + ") Rest of the messages too old/pinned. Stopping.");
				return;
			}
		});
	}
	
	
	
	
	
	class CleanTask extends TimerTask {

		private Timer timer;
		
		public CleanTask(Timer timer) {
			this.timer = timer;
		}
		
		@Override
		public void run() {
			//clean stuff here
			clean();
			//cancel old timer, start a new timer with days + 1
			this.timer.cancel();
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
