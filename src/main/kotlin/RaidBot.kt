import cleaner.CleanManager
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.reaction.ReactionEmoji
import io.CommandHandler
import model.Server
import raid.RaidMessageHandler

class RaidBot(token: String) {

    companion object {
        lateinit var gateway: GatewayDiscordClient
        const val prefix: String = "!"
        var cleaners: MutableList<CleanManager> = mutableListOf()
        var handlers: MutableList<RaidMessageHandler> = mutableListOf()
    }

    init {
        val client = DiscordClient.create(token)
        val gtw = client.login().block()

        gtw?.let {
            gateway = it

            val testGuild = gateway.getGuildById(Snowflake.of(498168048717398016L)).block()
            testGuild?.let { guild ->
                val testServer = Server(
                        raidChannels = listOf(Snowflake.of(498168048717398019L)),
                        templateReactions = listOf(
                            ReactionEmoji.of(503269083953758265L, "1_", false),
                            ReactionEmoji.of(503269083731460107L, "2_", false),
                            ReactionEmoji.of(503269084075393024L, "3_", false)
                        ),
                        persistentChannels = listOf(Snowflake.of(579704943926050826L)),
                        remoteReactions = listOf(),
                        guild = guild,
                        cleanChannels = listOf(Snowflake.of(498171854742355968L))
                )
                //handlers for "test server"
                val huutisHandler = RaidMessageHandler(testServer)
                handlers.plusAssign(huutisHandler)
                cleaners.plusAssign(CleanManager(testServer, huutisHandler))


                //pogo espoo

                val pogoGuild = gateway.getGuildById(Snowflake.of(483647882587537408L)).block()
                pogoGuild?.let { g1 ->
                    val pogoServer = Server(
                            raidChannels = listOf(
                                    // testatkaa toimintaa kannu
                                    Snowflake.of(497132570924810261L),
                                    // 5 raids
                                    Snowflake.of(497128671237373959L),
                                    // 4 raids
                                    Snowflake.of(497128696000544768L),
                                    // 1-2-3 raidit
                                    Snowflake.of(497128710097600512L)
                            ),
                            persistentChannels = listOf(
                                    // ex raids
                                    Snowflake.of(483648699499806720L),
                                    // community day ch (erikoispäivät)
                                    Snowflake.of(487710843639824404L)
                            ),
                            templateReactions = listOf(
                                    ReactionEmoji.of(496747453132046357L, "1_", false),
                                    ReactionEmoji.of(496747481196003331L, "2_", false),
                                    ReactionEmoji.of(496747490486255635L, "3_", false)
                            ),
                            remoteReactions = listOf(
                                    ReactionEmoji.of(705028426188455946L, "remote_raid", false),
                                    ReactionEmoji.of(705028444907634750L, "remote_raid2", false),
                                    ReactionEmoji.of(705028461042991194L, "remote_raid3", false)
                            ),
                            guild = g1,
                            cleanChannels = listOf(
                                    // raidikutsut
                                    Snowflake.of(735130568433467502L),
                                    // havainnot
                                    Snowflake.of(497129234670813187L)
                            )
                    )
                    // handlers for pogo espoo
                    val pogoHandler = RaidMessageHandler(pogoServer)
                    handlers.plusAssign(pogoHandler)
                    cleaners.plusAssign(CleanManager(pogoServer, pogoHandler))
                }

            }


            println("Gateway created")

            CommandHandler()

            gateway.onDisconnect().block()
        }
    }
}