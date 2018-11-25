package model;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class Raid {
	
	private IMessage raidMessage;
	//not sure if I need the channel
	private IChannel channel;
	private RaiderList rl;
	
	public Raid() {
		this.rl = new RaiderList();
	}
	
	public IMessage getIMessage() {
		return this.raidMessage;
	}
	
	public void setIMessage(IMessage message) {
		this.raidMessage = message;
	}
	
	public IChannel getIChannel() {
		return this.channel;
	}
	
	public void setIChannel(IChannel channel) {
		this.channel = channel;
	}
	
	public void removeMessage() {
		this.raidMessage.delete();
	}
	
	public RaiderList getRaiderList() {
		return this.rl;
	}
	
	public void setRaiderList(RaiderList rl) {
		this.rl = rl;
	}
	
	public String getRaidLocation() {
		return this.rl.getRaidLocation();
	}
	
	public void addRaider(String name) {
		this.rl.addRaider(name);
	}
	
	public void removeRaider(String name) {
		this.rl.removeRaider(name);
	}
	
	public EmbedObject getEmbedObject() {
		return this.rl.getEmbedObject();
	}
}
