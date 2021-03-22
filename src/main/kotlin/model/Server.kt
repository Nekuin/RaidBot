package model

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.reaction.ReactionEmoji

data class Server(
    var raidChannels: List<Snowflake>,
    var persistentChannels: List<Snowflake>,
    /**
     * You only need to specify extra channels in cleanChannels
     * all raidChannels will automatically get added to cleanChannels
     */
    var cleanChannels: List<Snowflake> = listOf(),
    /**
     * You should pass your emojis in count order E.g.
     * 1. emoji will count the user 1 times in the raid
     * 2. emoji will count the user 2 times in the raid
     * or just pass your own countMap instance if you don't want to utilize this automated version
     * same goes for remoteReactions
     */
    var templateReactions: List<ReactionEmoji>,
    var remoteReactions: List<ReactionEmoji>,
    var countMap: MutableMap<ReactionEmoji, Int> = mutableMapOf(),
    val guild: Guild
) {
    init {
        println("Initializing guild ${guild.name}")
        if(countMap.isEmpty()) {
            println("countMap empty, populating with template and remote reactions")

            var count = 1
            templateReactions.forEach { emoji ->
                countMap.plusAssign(Pair(emoji, count))
                count++
            }

            count = 1
            remoteReactions.forEach { emoji ->
                countMap.plusAssign(Pair(emoji, count))
                count++
            }
        }
        // include raidChannels in cleanChannels
        cleanChannels += raidChannels
        println("Clean channels: $cleanChannels")
    }

    fun excludeFromCleaning(channel: Snowflake) {
        cleanChannels -= channel
    }
}