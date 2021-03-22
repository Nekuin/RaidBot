package model

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import java.time.Instant

data class Raid(
        var message: Message,
        val channel: MessageChannel? = message.channel.block(),
        val raiderList: MutableList<String> = mutableListOf(),
        val timestamp: Long = System.currentTimeMillis(),
        val location: String,
        var time: String,
        var boss: String
){

    fun addRaider(raider: String, count: Int) {
        for(x in 1..count) {
            raiderList.add(raider)
        }
    }

    fun removeRaider(raider: String, count: Int) {
        for(x in 1..count) {
            raiderList.remove(raider)
        }
    }

    fun addRemoteRaider(raider: String, count: Int) {
        for(x in 1..count) {
            raiderList.add("$raider (etä)")
        }
    }

    fun removeRemoteRaider(raider: String, count: Int) {
        for(x in 1..count) {
            raiderList.remove("$raider (etä)")
        }
    }

    private fun createRaiderString(): String {
        var string = ""
        if(raiderList.isEmpty()) {
            string += "Ei ilmoittautuneita."
        } else {
            var i = 1
            raiderList.forEach{ name ->
                string += "$i $name \n"
                i++
            }
        }
        return string
    }

    fun sendRaidMessage(): Message? {
        return channel?.createEmbed { spec ->
            spec
                .setTitle("")
                .addField("Aika", time, true)
                .addField("Boss", boss, true)
                .addField("Paikka", location, false)
                .addField("Ilmoittautuneet", createRaiderString(), false)
                .setFooter("Kysy apua: !help", "")
                .setTimestamp(Instant.ofEpochMilli(timestamp))
        }?.block()
    }

    fun editRaidMessage() {
        message.edit { spec ->
            spec.setEmbed { emb ->
                emb
                    .setTitle("")
                    .addField("Aika", time, true)
                    .addField("Boss", boss, true)
                    .addField("Paikka", location, false)
                    .addField("Ilmoittautuneet", createRaiderString(), false)
                    .setFooter("Kysy apua: !help", "")
                    .setTimestamp(Instant.ofEpochMilli(timestamp))
            }
        }.subscribe()
    }

    override fun equals(other: Any?): Boolean {
        if(javaClass != other?.javaClass) return false
        if(this !== other) return false

        if(other.location != this.location) return false

        return true
    }

    override fun toString(): String {
        return "Location: $location, time: $time, boss: $boss, member count: ${raiderList.size} (msg: ${message.id} / ch:${channel?.id})"
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (channel?.hashCode() ?: 0)
        result = 31 * result + raiderList.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + boss.hashCode()
        return result
    }

}