package com.test.prox.console

import com.github.ajalt.clikt.core.*

class Console : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) = Console()
    .subcommands(
        Database().getCommands()
    ).main(args)