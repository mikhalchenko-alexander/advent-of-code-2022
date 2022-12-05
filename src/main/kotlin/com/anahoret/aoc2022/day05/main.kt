package com.anahoret.aoc2022.day05

import java.io.File
import kotlin.collections.ArrayDeque

private fun <E> ArrayDeque<E>.removeFirst(count: Int): List<E> {
    val result = mutableListOf<E>()
    repeat(count) {
        result.add(removeFirst())
    }
    return result
}

data class Move(val from: Int, val to: Int, val count: Int) {

    companion object {

        private val regex = "move (\\d+) from (\\d+) to (\\d+)".toRegex()
        fun parse(str: String): Move {
            val (count, from, to) = regex.find(str)!!.groupValues.drop(1).map(String::toInt)
            return Move(from, to, count)
        }
    }
}

class Stacks(private val stacks: List<ArrayDeque<String>>) {

    companion object {

        fun parse(str: String): Stacks {
            val stacks = str.lines()
                .dropLast(1)
                .map { it.windowed(size = 3, step = 4).map(::extractSymbol) }
                .let(::toStacks)
            return Stacks(stacks)
        }

        private fun toStacks(rows: List<List<String>>): List<ArrayDeque<String>> {
            val stacks = List(9) { ArrayDeque<String>() }
            rows.forEach { row ->
                row.forEachIndexed { idx, symbol ->
                    if (symbol.isNotBlank()) {
                        stacks[idx].addLast(symbol)
                    }
                }
            }
            return stacks
        }
        private fun extractSymbol(crate: String): String {
            return crate[1].toString()
        }
    }

    fun moveAll9000(moves: List<Move>): Stacks {
        val newStacks = stacks.map(::ArrayDeque)
        moves.forEach { move9000(it, newStacks) }
        return Stacks(newStacks)
    }

    fun moveAll9001(moves: List<Move>): Stacks {
        val newStacks = stacks.map(::ArrayDeque)
        moves.forEach { move9001(it, newStacks) }
        return Stacks(newStacks)
    }

    fun top(): String {
        return stacks.joinToString(separator = "") { it.firstOrNull() ?: " " }
    }

    private fun move9000(move: Move, stacks: List<ArrayDeque<String>>) {
        repeat(move.count) {
            val symbol = stacks[move.from - 1].removeFirst()
            stacks[move.to - 1].addFirst(symbol)
        }
    }

    private fun move9001(move: Move, stacks: List<ArrayDeque<String>>) {
        stacks[move.from - 1]
            .removeFirst(move.count)
            .reversed()
            .forEach(stacks[move.to - 1]::addFirst)
    }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day05/input.txt")
        .readText()
        .trim()

    val (stacksInput, moveInput) = input.split("\n\n")

    val stacks = stacksInput
        .let(Stacks.Companion::parse)

    val moves = moveInput
        .split("\n")
        .map(Move.Companion::parse)

    // Part 1
    println(stacks.moveAll9000(moves).top())

    // Part 2
    println(stacks.moveAll9001(moves).top())
}
