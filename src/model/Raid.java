package model;

import java.util.function.Consumer;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

public class Raid {
	
	private Message raidMessage;
	private RaiderList raiderList;
	private MessageChannel channel;
	
	/**
	 * Get the Discord message
	 * @return
	 */
	public Message getMessage() {
		return raidMessage;
	}
	
	/**
	 * Set the Discord message
	 * @param message
	 */
	public void setMessage(Message message) {
		raidMessage = message;
	}
	
	/**
	 * Get the channel the raid message is in
	 * @return
	 */
	public MessageChannel getChannel() {
		return channel;
	}
	
	/**
	 * Set the channel the raid message is in
	 * @param channel
	 */
	public void setChannel(MessageChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * Get the RaiderList
	 * @return
	 */
	public RaiderList getRaiderList() {
		return raiderList;
	}
	
	/**
	 * Set the RaiderList
	 * @param raiderList
	 */
	public void setRaiderList(RaiderList raiderList) {
		this.raiderList = raiderList;
	}
	
	/**
	 * Delete the Discord message
	 */
	public void removeMessage() {
		raidMessage.delete().block();
	}
	
	/**
	 * Get the Raid location
	 * @return
	 */
	public String getLocation() {
		return raiderList.getLocation();
	}
	
	/**
	 * Add a raider to the RaiderList
	 * @param raider
	 */
	public void addRaider(String raider) {
		raiderList.addRaider(raider);
	}
	
	/**
	 * Add <count> raiders to the RaiderList
	 * @param raider
	 * @param count
	 */
	public void addRaider(String raider, int count) {
		for(int i = 0; i < count; i++) {
			raiderList.addRaider(raider);
		}
	}
	
	/**
	 * Remove a raider from the RaiderList
	 * @param raider
	 */
	public void removeRaider(String raider) {
		raiderList.removeRaider(raider);
	}
	
	/**
	 * Remove <count> raiders from the RaiderList
	 * @param raider
	 * @param count
	 */
	public void removeRaider(String raider, int count) {
		for(int i = 0; i < count; i++) {
			raiderList.removeRaider(raider);
		}
	}
	
	/**
	 * Get the build EmbedObject for the raid message
	 * @return
	 */
	public Consumer<EmbedCreateSpec> getEmbedObject() {
		return raiderList.getEmbedObject;
	}
}
