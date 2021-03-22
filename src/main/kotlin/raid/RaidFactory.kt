package raid

import discord4j.core.`object`.entity.Message
import model.Raid
import java.lang.NumberFormatException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object RaidFactory {

    fun createRaid(message: Message): Raid? {
        val split = message.content.split(" ")
        return if(split.size >= 4) {
            var time = split[1]
            println("time str $time")
            if(time.trim() == "") {
                println("extra space in time detected, returning null")
                return null
            }
            if(time.startsWith("+")) {
                time = try {
                    val minutes = time.substring(1, time.length).toLong()
                    val then = LocalTime.now().plusMinutes(minutes)
                    val thenStr = then.format(DateTimeFormatter.ofPattern("HH:mm"))
                    thenStr
                } catch(e: NumberFormatException) {
                    println("${LocalTime.now()} Error parsing time from: $time")
                    val now = LocalTime.now()
                    val nowStr = now.format(DateTimeFormatter.ofPattern("HH:mm"))
                    nowStr
                }

            }
            val boss = split[2]
            if(boss.trim() == "") {
                println("extra space in boss detected, returning null")
                return null
            }
            var location = ""
            for(slice in 3 until split.size) {
                location += "${split[slice]} "
            }
            location = location.trim()
            Raid(message = message,
                time = time,
                location = location,
                boss = boss)
        } else {
            println("probably invalid raid message")
            null
        }
    }

}