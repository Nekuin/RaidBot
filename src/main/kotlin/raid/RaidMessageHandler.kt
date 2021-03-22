package raid

import RaidBot
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import model.Raid
import model.Server
import java.lang.Integer.min
import java.lang.Integer.parseInt
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class RaidMessageHandler(private val server: Server) {

    // raids that can (and will be) cleaned
    private val raidList: MutableList<Raid> = mutableListOf()
    // raids that will not be cleaned
    private val persistentRaidList: MutableList<Raid> = mutableListOf()

    init {
        // listen for raid commands
        RaidBot.gateway
            .on(MessageCreateEvent::class.java)
            .map(MessageCreateEvent::getMessage)
            .filter{message -> server.raidChannels.contains(message.channelId)}
            .filter{message -> message.content.startsWith(RaidBot.prefix + "raid")}
            .subscribe{ m -> this.onRaidMessage(m, raidList)}

        // listen for raid commands in persistent channels
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.persistentChannels.contains(message.channelId) }
                .filter{ message -> message.content.startsWith(RaidBot.prefix + "raid")}
                .subscribe{ m -> this.onRaidMessage(m, persistentRaidList)}

        // reaction add event (normal reactions)
        RaidBot.gateway
            .on(ReactionAddEvent::class.java)
            .filter{event -> server.raidChannels.contains(event.channelId)}
            .filterWhen { event -> event.user.map { user -> !user.isBot } }
            .filter { event -> server.templateReactions.contains(event.emoji) }
            .subscribe(this::onReactionAdd)

        // reaction add event (persistent raid)
        RaidBot.gateway
                .on(ReactionAddEvent::class.java)
                .filter{event -> server.persistentChannels.contains(event.channelId)}
                .filterWhen { event -> event.user.map { user -> !user.isBot } }
                .filter { event -> server.templateReactions.contains(event.emoji) }
                .subscribe(this::onReactionAdd)

        // reaction remove event (normal reactions)
        RaidBot.gateway
            .on(ReactionRemoveEvent::class.java)
            .filter { event -> server.raidChannels.contains(event.channelId) }
            .filterWhen { event -> event.user.map { user -> !user.isBot}}
            .filter{ event -> server.templateReactions.contains(event.emoji) }
            .subscribe(this::onReactionRemove)

        // reaction remove event (persistent raid)
        RaidBot.gateway
                .on(ReactionRemoveEvent::class.java)
                .filter { event -> server.persistentChannels.contains(event.channelId) }
                .filterWhen { event -> event.user.map { user -> !user.isBot}}
                .filter{ event -> server.templateReactions.contains(event.emoji) }
                .subscribe(this::onReactionRemove)

        // reaction add event (remote reactions)
        RaidBot.gateway
                .on(ReactionAddEvent::class.java)
                .filter { event -> server.raidChannels.contains(event.channelId) }
                .filterWhen { event -> event.user.map { user -> !user.isBot} }
                .filter { event -> server.remoteReactions.contains(event.emoji) }
                .subscribe(this::onRemoteReactionAdd)

        // reaction add event (remote raid, persistent raid)
        RaidBot.gateway
                .on(ReactionAddEvent::class.java)
                .filter { event -> server.persistentChannels.contains(event.channelId) }
                .filterWhen { event -> event.user.map { user -> !user.isBot} }
                .filter { event -> server.remoteReactions.contains(event.emoji) }
                .subscribe(this::onRemoteReactionAdd)

        // reaction remove event (remote reactions)
        RaidBot.gateway
                .on(ReactionRemoveEvent::class.java)
                .filter { event -> server.raidChannels.contains(event.channelId) }
                .filterWhen { event -> event.user.map { user -> !user.isBot} }
                .filter { event -> server.remoteReactions.contains(event.emoji) }
                .subscribe(this::onRemoteReactionRemove)

        // reaction remove event (remote reactions, persistent raid)
        RaidBot.gateway
                .on(ReactionRemoveEvent::class.java)
                .filter { event -> server.persistentChannels.contains(event.channelId) }
                .filterWhen { event -> event.user.map { user -> !user.isBot} }
                .filter { event -> server.remoteReactions.contains(event.emoji) }
                .subscribe(this::onRemoteReactionRemove)

        // time edit commands
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.raidChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "aika") }
                .subscribe(this::onTimeEditMessage)

        // time edit commands (persistent raids)
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.persistentChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "aika") }
                .subscribe(this::onTimeEditMessage)

        // boss edit commands
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.raidChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "boss") }
                .subscribe(this::onBossEditMessage)

        // boss edit commands (persistent raids)
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.persistentChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "boss") }
                .subscribe(this::onBossEditMessage)

        // help message
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.raidChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "help") }
                .subscribe(this::onHelpMessage)

        // help message (persistent channels)
        RaidBot.gateway
                .on(MessageCreateEvent::class.java)
                .map(MessageCreateEvent::getMessage)
                .filter { message -> server.persistentChannels.contains(message.channelId) }
                .filter { message -> message.content.startsWith(RaidBot.prefix + "help") }
                .subscribe(this::onHelpMessage)

    }

    private fun onRaidMessage(message: Message, list: MutableList<Raid>) {
        val channel = message.channel.block() as GuildMessageChannel
        val guild = message.guild.block()?.name
        println("${LocalTime.now()} Raid message received ($guild/${channel.name}): ${message.content}")
        val raid = RaidFactory.createRaid(message)
        raid?.let {
            findAndRemoveDuplicates(raid)
            list += it
            println("Added raid to list, $list")
            val msg = it.sendRaidMessage()
            // the already set message is actually wrong, lets set the correct one
            msg?.let { m -> raid.message = m }
            server.templateReactions.forEach{ emoji ->
                msg?.addReaction(emoji)?.block()
            }
            server.remoteReactions.forEach { emoji ->
                msg?.addReaction(emoji)?.block()
            }
            message.delete().block()
        }
        if(raid == null) {
            sendRaidHelpMessage(message)
            message.delete().doOnError { e ->
                println("Couldn't delete message $e")
            }.block()
        }
    }

    private fun findRaidById(raidId: Snowflake): Raid? {
        println("Searching for raid by id")
        raidList.forEach { raid ->
            if(raid.message.id.asLong() == raidId.asLong()) {
                println("Raid found with id ${raidId.asLong()}")
                return raid
            }
        }
        persistentRaidList.forEach { raid ->
            if(raid.message.id.asLong() == raidId.asLong()) {
                println("Persistent raid found with id ${raidId.asLong()}")
                return raid
            }
        }
        println("Raid not found with id ${raidId.asLong()}")
        return null
    }

    private fun findRaidByLocation(location: String): Raid? {
        println("Searching for raid by location")
        raidList.forEach { raid ->
            if(raid.location == location) {
                println("Raid found with location $location")
                return raid
            }
        }
        persistentRaidList.forEach { raid ->
            if(raid.location == location) {
                println("Persistent raid found with location $location");
                return raid
            }
        }
        println("Raid not found with location $location")
        return null
    }

    private fun findAndRemoveDuplicates(raid: Raid) {
        var duplicate: Raid? = null
        if(persistentRaidList.contains(raid)) {
            persistentRaidList.forEach { r ->
                println("Persistent duplicate found $raid")
                duplicate = r
            }
        } else {
            raidList.forEach { r ->
                if(raid.location == r.location) {
                    // need to make sure channels match too
                    if(raid.channel == r.channel) {
                        duplicate = r
                        println("Duplicate found $r")
                    }
                }
            }
        }
        duplicate?.let {
            it.message.delete().subscribe()
            raidList -= it
            persistentRaidList -= it
        }
    }

    private fun getRaiderName(member: Member): String {
        return if(member.nickname.isPresent) member.nickname.get() else member.displayName
    }

    private fun onReactionAdd(addEvent: ReactionAddEvent) {
        val guild = addEvent.guild.block()?.name
        val channel = addEvent.channel.block() as GuildMessageChannel
        println("${LocalTime.now()} Reaction added ($guild/${channel.name})")
        val raid = findRaidById(addEvent.messageId)
        raid?.let {
            val user: Member? = server.guild.getMemberById(addEvent.userId).block()
            user?.let { member ->
                val name = getRaiderName(member)
                server.countMap[addEvent.emoji]?.let { it1 ->
                    println("Everything found, editing message (Location: ${it.location})")
                    raid.addRaider(name, it1)
                    raid.editRaidMessage()
                }
            }
        }
    }

    private fun onRemoteReactionAdd(addEvent: ReactionAddEvent) {
        val guild = addEvent.guild.block()?.name
        val channel = addEvent.channel.block() as GuildMessageChannel
        println("${LocalTime.now()} Reaction added ($guild/${channel.name})")
        val raid = findRaidById(addEvent.messageId)
        raid?.let {
            val user: Member? = server.guild.getMemberById(addEvent.userId).block()
            user?.let { member ->
                val name = getRaiderName(member)
                server.countMap[addEvent.emoji]?.let { it1 ->
                    println("Everything found, editing message (Location: ${it.location})")
                    raid.addRemoteRaider(name, it1)
                    raid.editRaidMessage()
                }
            }
        }
    }

    private fun onRemoteReactionRemove(removeEvent: ReactionRemoveEvent) {
        val guild = removeEvent.guild.block()?.name
        val channel = removeEvent.channel.block() as GuildMessageChannel
        println("${LocalTime.now()} Reaction removed ($guild/${channel.name})")
        val raid = findRaidById(removeEvent.messageId)
        raid?.let {
            val user: Member? = server.guild.getMemberById(removeEvent.userId).block()
            user?.let { member ->
                val name = getRaiderName(member)
                server.countMap[removeEvent.emoji]?.let { it1 ->
                    println("Everything found, editing message (Location: ${it.location})")
                    raid.removeRemoteRaider(name, it1)
                    raid.editRaidMessage()
                }
            }
        }
    }

    private fun onReactionRemove(removeEvent: ReactionRemoveEvent) {
        val guild = removeEvent.guild.block()?.name
        val channel = removeEvent.channel.block() as GuildMessageChannel
        println("${LocalTime.now()} Reaction removed ($guild/${channel.name})")
        val raid = findRaidById(removeEvent.messageId)
        raid?.let {
            val user: Member? = server.guild.getMemberById(removeEvent.userId).block()
            user?.let { member ->
                val name = getRaiderName(member)
                server.countMap[removeEvent.emoji]?.let {it1 ->
                    println("Everything found, editing message (Location: ${it.location})")
                    raid.removeRaider(name, it1)
                    raid.editRaidMessage()
                }
            }
        }
    }

    private fun onTimeEditMessage(message: Message) {
        val channel = message.channel.block() as GuildMessageChannel
        val guild = message.guild.block()?.name
        println("${LocalTime.now()} Time edit ($guild/${channel.name}): ${message.content}")
        val content = message.content.split(" ")
        // 3 or more = !raid TIME LOCATION (LOCATION)
        if(content.size >= 3) {
            var location = ""
            for(slice in 2 until content.size) {
                location += "${content[slice]} "
            }
            location = location.trim()
            val raid = findRaidByLocation(location)

            var time = content[1]
            // attempt to parse +x min time
            if(time.startsWith("+")) {
                val oldTime = raid?.time
                oldTime?.let {
                    try {
                        val mins = parseInt(time.substring(1, time.length))
                        val attempt = tryParsePlusTime(mins, oldTime)
                        if(attempt != null) {
                            println("Parse most likely successful $attempt")
                            time = attempt
                        }
                    } catch(e: NumberFormatException) {
                        println("minutes to add parse failed")
                    }
                }
            }

            raid?.let {
                raid.time = time
                raid.editRaidMessage()
            }
        } else {
            // invalid command
            println("${LocalTime.now()} Invalid time edit ($guild/${channel.name}): ${message.content}")
        }
        message.delete().subscribe()
    }

    private fun onBossEditMessage(message: Message) {
        val channel = message.channel.block() as GuildMessageChannel
        val guild = message.guild.block()?.name
        println("${LocalTime.now()} Boss edit ($guild/${channel.name}): ${message.content}")
        val content = message.content.split(" ")
        // 3 or more = !raid BOSS LOCATION (LOCATION)
        if(content.size >= 3) {
            val boss = content[1]
            var location = ""
            for(slice in 2 until content.size) {
                location += "${content[slice]} "
            }
            location = location.trim()
            val raid = findRaidByLocation(location)
            raid?.let {
                raid.boss = boss
                raid.editRaidMessage()
            }
        } else {
            // invalid command
            println("${LocalTime.now()} Invalid boss edit ($guild/${channel.name}): ${message.content}")
        }
        message.delete().subscribe()
    }

    private fun sendRaidHelpMessage(message: Message) {
        println("${LocalTime.now()} [CREATE RAID] - Problem creating a raid, sending instructions.")
        val helpMessage = """
            Jotain puuttui ${RaidBot.prefix}raid komennostasi...
            ```${message.content}```Koita näin: 
            ```${RaidBot.prefix}raid AIKA BOSS PAIKKA
            ${RaidBot.prefix}raid 12:00 mew suvelan tammi``` 
            Something was missing from your ${RaidBot.prefix}raid command... 
            Try this: 
            ```${RaidBot.prefix}raid TIME BOSS LOCATION```
            ``Yleisin syy komennon toimimattomuuteen on extra välilyönti.``
            ``The most common problem is an extra space character.``
            """.trimIndent()
        // send help message
        message.author.get().privateChannel.block()
                ?.createMessage(helpMessage)?.block()
    }

    private fun onHelpMessage(message: Message) {
        println("${LocalTime.now()} Help message requested, sending")
        val helpMessage = """
            **Create a raid**:
            ```${RaidBot.prefix}raid STARTINGTIME BOSS LOCATION
            i.e. ${RaidBot.prefix}raid 12.00 Mewtwo Suvelan Tammi```
            Join a raid:
            ```Click on reactions +1, +2 or +3, the number indicates how many devices(or persons) you sign up for the raid```
            Fix/change the time on a raid
            ```${RaidBot.prefix}aika TIME LOCATION
            i.e. ${RaidBot.prefix}aika 15:00 Suvelan Tammi```
            Change boss's name on a raid:
            ```${RaidBot.prefix}boss BOSS LOCATION
            i.e. ${RaidBot.prefix}boss mew Suvelan Tammi```
            Can't see the bot's message? Make sure link preview is enabled.
            Questions or feedback? Contact Nekuin#3936 on discord.
            
            **Ilmoita uusi raidi**: 
            ```${RaidBot.prefix}raid ALOITUSAIKA BOSSI PAIKKA
            ESIM: ${RaidBot.prefix}raid 12.00 Mewtwo Suvelan Tammi```
            Ilmoita itsesi raidiin: 
            ```Klikkaa reaktioita +1 +2 tai +3, numero kertoo kuinka monta ilmoitat```
            Korjaa aika raidiin: 
            ```${RaidBot.prefix}aika AIKA PAIKKA
            ESIM: ${RaidBot.prefix}aika 15:00 Suvelan Tammi```
            Vaihda bossin nimi raidiin: 
            ```${RaidBot.prefix}boss BOSS PAIKKA
            ESIM: ${RaidBot.prefix}boss mew Suvelan Tammi```
            Etkö näe botin viestiä? Varmista että linkkien esikatselu on päällä.
            Kysymykset ja palautteet voi laittaa Discordissa Nekuin#3936.
            """.trimIndent()
        // send help message
        message.author.get().privateChannel.block()
                ?.createMessage(helpMessage)?.block()
        // message is already deleted in onRaidMessage
        // no need to delete it here
    }

    /**
     * Judging from old bots usage, the separator between HH and MM is nothing (""), ":" or "."
     * I'll also include white space (" ") just in case
     */
    private fun tryParsePlusTime(minsToAdd: Int, oldTime: String): String? {
        println("plus parser, minutes to add: $minsToAdd, old time: $oldTime")
        // 4 length could be 1200 (without anything between HH and MM) or e.g. 9:30
        if(oldTime.length == 4) {
            try {
                parseInt(oldTime)
                // just numbers, it's probably 1200 or something like that
                val hours = parseInt(oldTime.substring(0, 2))
                val mins = parseInt(oldTime.substring(2, oldTime.length))
                return SimpleDateFormat("hh:mm").format(
                        Calendar.getInstance().also { date ->
                            date.set(Calendar.HOUR_OF_DAY, hours)
                            date.set(Calendar.MINUTE, mins)
                            val oldMins = date.get(Calendar.MINUTE)
                            date.set(Calendar.MINUTE, oldMins + minsToAdd)
                        }.time
                )
            } catch(e: NumberFormatException) {
                // not only numbers, so it's probably 9:30 or something like that
                if(":" in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, ":")
                } else if("." in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, ".")
                } else if(" " in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, " ")
                } else {
                    return null
                }
            }
        } else if(oldTime.length == 5) {
            try {
                parseInt(oldTime)
                // just numbers, don't know what to expect.
                return null
            } catch(e: NumberFormatException) {
                // not only numbers, so it's probably 19:30 or something like that
                if(":" in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, ":")
                } else if("." in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, ".")
                } else if(" " in oldTime) {
                    return getFormattedTime(oldTime, minsToAdd, " ")
                } else {
                    // some other splitter.. don't know what that would be
                    return null
                }
            }
        }
        return null
    }

    private fun getFormattedTime(oldTime: String, minsToAdd: Int, splitter: String): String {
        return SimpleDateFormat("hh:mm").format(
                Calendar.getInstance().also { date ->
                    val hm = splitTime(splitter, oldTime)
                    val oldMins = hm[1]
                    val oldHours = hm[0]
                    date.set(Calendar.HOUR_OF_DAY, oldHours)
                    date.set(Calendar.MINUTE, oldMins)
                    val mins = date.get(Calendar.MINUTE)
                    date.set(Calendar.MINUTE, mins + minsToAdd)
                }.time
        )
    }

    private fun splitTime(splitter: String, oldTime: String): Array<Int> {
        val newTime = oldTime.split(splitter)
        val addHours = parseInt(newTime[0])
        val addMinutes = parseInt(newTime[1])
        return arrayOf(addHours, addMinutes)
    }

    fun clearRaids() {
        raidList.clear()
    }

    fun printRaids() {
        println("Server: ${server.guild.name}")
        println("Normal raids:")
        raidList.forEach { r -> println(r) }
        println("Persistent raids:")
        persistentRaidList.forEach { r -> println(r) }
    }
}