package com.anahoret.aoc2022.day20

import java.io.File
import kotlin.math.abs
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
        .sumOf { numbers[calculateEndIndex(zeroIdx, it, numbers.size)].value }
}

private fun mix(numbers: Array<NumberContainer>) {
    numbers.indices.forEach { idx ->
        val currentIndex = numbers.indexOfFirst { it.initialIndex == idx }
        numbers.shift(currentIndex, numbers[currentIndex].value)
    }
}

private fun <T> Array<T>.shift(idx: Int, amount: Long) {
    val endIndex = calculateShiftCycledIndex(idx, amount, size)
    val tmp = this[idx]
    when {
        endIndex > idx -> (idx until endIndex).forEach { i -> this[i] = this[i + 1] }
        else -> (idx downTo endIndex.inc()).forEach { i -> this[i] = this[i - 1] }
    }
    this[endIndex] = tmp
}

private fun calculateEndIndex(idx: Int, amount: Int, size: Int): Int {
    val endIndex = idx + amount
    return when {
        endIndex in 0 until size -> endIndex
        endIndex < 0 -> (size + endIndex % size) % size
        else -> endIndex % size
    }
}

private fun calculateShiftCycledIndex(idx: Int, amount: Long, size: Int): Int {
    val endIndex = idx + amount
    return when {
        endIndex == 0L -> size - 1
        endIndex == size - 1L -> 0
        endIndex in 0 until size -> endIndex.toInt()
        endIndex < 0 -> {
            val transfers = abs(endIndex / size.dec()) + 1
            (size + (endIndex - transfers) % size) % size
        }
        else -> {
            val transfers = endIndex / size.dec()
            (endIndex + transfers) % size
        }
    }.toInt()
}
