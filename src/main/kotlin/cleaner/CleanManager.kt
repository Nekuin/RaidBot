package cleaner

import discord4j.core.`object`.entity.channel.GuildMessageChannel
import model.Server
import raid.RaidMessageHandler

class CleanManager(server: Server, private val handler: RaidMessageHandler) {

    private val cleanTasks: MutableList<ChannelCleaner> = mutableListOf()

    init {
        println("[CleanManager] Starting... (${server.guild.name})")
        Thread {
            server.cleanChannels.forEach{ chId ->
                val channel = RaidBot.gateway.getChannelById(chId).block() as GuildMessageChannel
                cleanTasks += ChannelCleaner(channel, handler)
            }
        }.start()
    }

    fun cleanNow() {
        cleanTasks.forEach { task ->
            task.cleanNow()
        }
        println("Clearing raids..")
        handler.clearRaids()
    }

    fun printCleaningTimes() {
        cleanTasks.forEach { task ->
            task.printCleanTime()
        }
    }
}