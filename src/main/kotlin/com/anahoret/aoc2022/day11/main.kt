package com.anahoret.aoc2022.day11

import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.floor

class Monkey(
    startingItems: List<Long>,
    val testNumber: Long,
    private val operation: (Long) -> Long,
    private val throwMap: Map<Boolean, Int>
) {

    var inspectCount = 0L
        private set

    private val items = startingItems.toMutableList()

    fun hasItems(): Boolean {
        return items.isNotEmpty()
    }

    fun inspectNextAndThrow(newCalculator: (Long) -> Long, targetMonkeyGetter: (Int) -> Monkey) {
        inspectCount++
        val old = items.removeAt(0)
        val new = newCalculator(operation(old))
        val targetMonkeyIdx = throwMap.getValue(new % testNumber == 0L)
        targetMonkeyGetter(targetMonkeyIdx).items.add(new)
    }

    companion object {
        fun parse(str: String): Monkey {
            val lines = str.lines()
            val startingItems = lines[1].split(": ")[1]
                .split(", ")
                .map(String::toLong)
            val operation = lines[2].takeLastWhile { it != '=' }.trim().split(" ")
                .let { (a, o, b) ->
                    { old: Long ->
                        val operand1 = if (a == "old") old else a.toLong()
                        val operand2 = if (b == "old") old else b.toLong()
                        val operation: (Long, Long) -> Long = when (o) {
                            "+" -> Long::plus
                            "*" -> Long::times
                            else -> throw IllegalArgumentException("Unknown operation: $o")
                        }
                        operation(operand1, operand2)
                    }
                }
            val testNumber = lines[3].takeLastWhile { it != ' ' }.toLong()
            val throwIfTrue = lines[4].takeLastWhile { it != ' ' }.toInt()
            val throwIfFalse = lines[5].takeLastWhile { it != ' ' }.toInt()
            val throwMap = mapOf(true to throwIfTrue, false to throwIfFalse)
            return Monkey(startingItems, testNumber, operation, throwMap)
        }
    }

}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day11/input.txt")
        .readText()
        .trim()
        .split("\n\n")

    // Part 1
    part1(input.map(Monkey.Companion::parse))

    // Part 2
    part2(input.map(Monkey.Companion::parse))
}

private fun part1(monkeys: List<Monkey>) {
    playMonkeyInTheMiddle(monkeys, 20) { new -> floor(new / 3.0).toLong() }
}

private fun part2(monkeys: List<Monkey>) {
    val testNumbersProduct = monkeys.fold(1L) { acc, m -> acc * m.testNumber }
    playMonkeyInTheMiddle(monkeys, 10000) { new -> new % testNumbersProduct }
}

fun playMonkeyInTheMiddle(
    monkeys: List<Monkey>,
    rounds: Int,
    newItemValueMapper: (Long) -> Long
) {
    repeat(rounds) {
        monkeys.forEach { monkey ->
            while (monkey.hasItems()) {
                monkey.inspectNextAndThrow(newItemValueMapper, monkeys::get)
            }
        }
    }

    monkeys
        .map(Monkey::inspectCount)
        .sorted()
        .takeLast(2)
        .reduce(Long::times)
        .let(::println)
}
