package io

import java.util.Scanner
import kotlin.system.exitProcess

class CommandHandler {

    private var looping = true

    init {
        Thread {
            println("Listening for commands")
            while(looping) {
                var cmd = ""
                with(Scanner(System.`in`)) {
                    cmd = readLine()!!
                    handleCommand(cmd)
                }
            }
            println("Stopped listening for commands")
        }.start()
    }

    private fun handleCommand(command: String) {
        when(command) {
            "stop" -> stop()
            "help" -> printHelp()
            "?" -> printHelp()
            "clean" -> cleanNow()
            "schedule" -> printCleanTimes()
            "raids" -> printRaids()
        }
    }

    private fun printCleanTimes() {
        RaidBot.cleaners.forEach {
            it.printCleaningTimes()
        }
    }

    private fun cleanNow() {
        RaidBot.cleaners.forEach { cleaner ->
            cleaner.cleanNow()
        }
    }

    private fun printRaids() {
        RaidBot.handlers.forEach { handler -> handler.printRaids()}
    }

    private fun printHelp() {
        println("Available commands")
        println("clean\t\tClean channels")
        println("schedule\tPrints cleaning times for all channels")
        println("raids\t\tPrint current raids in memory")
        println("stop\t\tLog out")
    }

    private fun stop() {
        looping = false
        RaidBot.gateway.logout().block()
        exitProcess(0)
    }
}