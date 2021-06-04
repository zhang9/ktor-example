package com.test.prox.console

import com.github.ajalt.clikt.core.*

class Database : CliktCommand() {
    override fun run() = Unit

    class Init : CliktCommand(help = "Initialize the database") {
        override fun run() {
            echo("Initialized the database.")
        }
    }

    class Drop : CliktCommand(help = "Drop the database") {
        override fun run() {
            echo("Dropped the database.")
        }
    }

    fun getCommands(): Database {
        return subcommands(Init(), Drop())
    }

}