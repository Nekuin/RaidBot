fun main(args: Array<String>) {

    if(args.isNotEmpty()) {
        println("args token ${args[0]}")
        val token = args[0]
        RaidBot(token)
    } else {
        // token
        val token = "NDk1Mjg4MTQ0ODYyNzczMjQ4.W65nBw.dvO8V04RhWEPPE5qQ1yFskNBx6E"
        RaidBot(token)
    }
}