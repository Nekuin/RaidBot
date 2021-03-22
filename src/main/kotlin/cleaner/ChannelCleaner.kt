package cleaner

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import raid.RaidMessageHandler
import java.time.Instant
import java.time.LocalTime
import java.util.*

class ChannelCleaner(private val channel: GuildMessageChannel,
                        private val handler: RaidMessageHandler) {

    private val twoWeeks: Instant = Instant.ofEpochSecond(Instant.now().epochSecond - 1209600)
    private val timer: Timer = Timer()
    private var schedule: Date

    init {
        println("Cleaner task started for channel ${channel.name}")
        schedule = getScheduleTime()
        timer.schedule(CleanTask(), schedule)
        println("First clean scheduled at $schedule")
    }

    private fun getScheduleTime(): Date {
        return Calendar.getInstance()
                .also {
                    val day = it.get(Calendar.DAY_OF_MONTH)
                    it.set(Calendar.DAY_OF_MONTH, day+1)
                    it.set(Calendar.HOUR_OF_DAY, 0)
                    it.set(Calendar.MINUTE, 1)
                    it.set(Calendar.SECOND, 0)
                }.time
    }

    private fun clean() {
        println("${LocalTime.now()} Cleaning started in ${channel.name}")
        // count things
        var count = channel.getMessagesBefore(Snowflake.of(Instant.now())).take(100)
                .filter { msg -> msg.timestamp.isAfter(twoWeeks) }
                .filter { msg -> !msg.isPinned }
                .map(Message::getId)
                .count()
                .block()
        while(count!! > 0) {
            removeBatch()
            count = channel.getMessagesBefore(Snowflake.of(Instant.now())).take(100)
                    .filter { msg -> msg.timestamp.isAfter(twoWeeks) }
                    .filter { msg -> !msg.isPinned }
                    .map(Message::getId)
                    .count()
                    .block()
        }
        println("${LocalTime.now()} Cleaning complete in ${channel.name}")
    }

    private fun removeBatch() {
        val messages = channel.getMessagesBefore(Snowflake.of(Instant.now())).take(100)
                .filter{ msg -> msg.timestamp.isAfter(twoWeeks)}
                .filter { msg -> !msg.isPinned }
                .map (Message::getId)
                .transform(channel::bulkDelete)

        val last = messages.blockLast()
        last?.let {
            channel.getMessageById(it).block()?.delete()?.block()
        }
    }

    fun cleanNow() {
        println("${LocalTime.now()} Cleaning started in ${channel.name}")
        clean()
    }

    fun printCleanTime() {
        println("${channel.name} cleaning scheduled at $schedule")
    }

    inner class CleanTask: TimerTask() {
        override fun run() {
            clean()
            println("${LocalTime.now()} Clearing raids...")
            handler.clearRaids()
            Timer().apply {
                //val time = Date(Calendar.getInstance().timeInMillis + (10 * 3600))
                schedule = getScheduleTime()
                println("New schedule in channel ${channel.name} $schedule")
                this.schedule(CleanTask(), schedule)
            }
        }

    }
}