package com.anahoret.aoc2022.day20

import com.anahoret.aoc2022.calculateEndIndex
import java.io.File
import kotlin.system.measureTimeMillis

data class NumberContainer(val initialIndex: Int, val value: Long)

fun parse(str: String): Array<NumberContainer> {
    return str.split("\n")
        .mapIndexed { idx, s -> NumberContainer(idx, s.toLong()) }
        .toTypedArray()
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day20/input.txt")
        .readText()
        .trim()

    val numbers = parse(input)

    // Part 1
    part1(numbers.copyOf()).also { println("P1: ${it}ms") }

    // Part 2
    part2(numbers.copyOf()).also { println("P2: ${it}ms") }
}

private fun part1(numbers: Array<NumberContainer>) = measureTimeMillis {
    mix(numbers)

    calcResult(numbers)
        .also(::println)
}

private fun part2(numbers: Array<NumberContainer>) = measureTimeMillis {
    val decryptionKey = 811589153
    val multipliedNumbers = numbers.map { it.copy(value = it.value * decryptionKey) }.toTypedArray()

    repeat(10) {
        mix(multipliedNumbers)
    }

    calcResult(multipliedNumbers)
        .also(::println)
}

private fun calcResult(numbers: Array<NumberContainer>): Long {
    val zeroIdx = numbers.indexOfFirst { it.value == 0L }
    return listOf(1000, 2000, 3000)
        .sumOf { numbers[calculateEndIndex(zeroIdx, it.toLong(), numbers.size)].value }
}

private fun mix(numbers: Array<NumberContainer>) {
    numbers.indices.forEach { idx ->
        val currentIndex = numbers.indexOfFirst { it.initialIndex == idx }
        numbers.shift(currentIndex, numbers[currentIndex].value)
    }
}

private fun <T> Array<T>.shift(idx: Int, amount: Long) {
    val endIndex = calculateEndIndex(idx, amount, size - 1)
    val tmp = this[idx]
    when {
        endIndex > idx -> (idx until endIndex).forEach { i -> this[i] = this[i + 1] }
        else -> (idx downTo endIndex.inc()).forEach { i -> this[i] = this[i - 1] }
    }
    this[endIndex] = tmp
}
