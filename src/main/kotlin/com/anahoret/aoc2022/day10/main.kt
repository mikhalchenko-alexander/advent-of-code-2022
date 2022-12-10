package com.anahoret.aoc2022.day10

import java.io.File

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day10/input.txt")
        .readText()
        .trim()

    val commandResults = input
        .split("\n")
        .map(Command.Companion::parse)
        .fold(State()) { state, comm -> state.run(comm) }
        .getCommandResults()

    // Part 1
    part1(commandResults)

    // Part 2
    part2(commandResults)
}

class State {
    private var current = CommandResult(0, 1)
    private val commandResults = mutableListOf<CommandResult>()

    fun run(command: Command): State {
        val result = when (command) {
            is Addx -> CommandResult(
                endsAfterTick = current.endsAfterTick + command.ticks,
                registerValueAfter = current.registerValueAfter + command.value
            )
            Noop -> CommandResult(
                endsAfterTick = current.endsAfterTick + command.ticks,
                registerValueAfter = current.registerValueAfter
            )
        }
        commandResults += result
        current = result
        return this
    }

    fun getCommandResults() = commandResults.toList()
}

sealed class Command(val ticks: Int) {

    companion object {
        fun parse(str: String): Command {
            return if (str.startsWith("noop")) Noop
            else Addx(str.split(" ")[1].toInt())
        }
    }

}

class Addx(val value: Int) : Command(ticks = 2)
object Noop : Command(ticks = 1)

class CommandResult(val endsAfterTick: Int, val registerValueAfter: Int)

private fun part1(commandResults: List<CommandResult>) {
    (20..220 step 40).sumOf { cycle ->
        commandResults.last { it.endsAfterTick < cycle }
            .registerValueAfter * cycle
    }.let(::println)
}

private fun part2(commandResults: List<CommandResult>) {
    val screenWidth = 40
    val screenHeight = 6
    val screen = Array(screenHeight) { Array(screenWidth) { ' ' } }
    (1..240).forEach { cycle ->
        val row = (cycle - 1) / screenWidth
        val col = (cycle - 1) % screenWidth

        val sprite = (commandResults.lastOrNull { it.endsAfterTick < cycle }?.registerValueAfter ?: 1)
            .let { (it - 1)..(it + 1) }

        if (col in sprite) screen[row][col] = '#'
    }
    println(screen.joinToString("\n") { it.joinToString(separator = "") })
}
