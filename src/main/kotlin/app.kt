fun main(args: Array<String>) {

    if(args.isNotEmpty()) {
        println("args token ${args[0]}")
        val token = args[0]
        RaidBot(token)
    } else {
        // token
        val token = "token goes here"
        RaidBot(token)
    }
}