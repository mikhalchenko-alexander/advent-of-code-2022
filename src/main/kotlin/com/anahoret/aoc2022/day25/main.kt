package com.anahoret.aoc2022.day25

import java.io.File
import kotlin.math.pow
import kotlin.system.measureTimeMillis

private val snafuCoding = mapOf(
    '2' to 2,
    '1' to 1,
    '0' to 0,
    '-' to -1,
    '=' to -2
)

private val reverseSnafuCoding = snafuCoding.map { (key, value) -> value to key }.toMap()

fun snafuToDec(snafu: String): Long {
    return snafu
        .map(snafuCoding::getValue)
        .reversed()
        .foldIndexed(0L) { pow, acc, c -> acc + (c * 5.pow(pow)) }
}

fun Int.pow(n: Int): Long {
    return this.toDouble().pow(n).toLong()
}

data class MulPower(val mul: Int, val power: Int)

fun decToSnafu(dec: Long): String {
    fun sumPower5(pow: Int, mul: Int): Long {
        return (0..pow).fold(0L) { acc, p -> acc + mul * 5.pow(p) }
    }

    fun loop(acc: List<MulPower> = mutableListOf()): List<Int> {
        val accVal = acc.fold(0L) { accV, (mul, pow) -> accV + mul * 5.pow(pow) }

        if (accVal == dec) {
            val minPow = acc.minOf(MulPower::power)
            val multipliers = acc.map(MulPower::mul)
            return if (minPow == 0) multipliers
            else multipliers + List(minPow) { 0 }
        }

        val nextPow = acc.minOfOrNull(MulPower::power)?.dec()
            ?: (0..Int.MAX_VALUE).first { 2 * 5.pow(it) >= dec }

        val nextMul = if (accVal > dec) {
            (-2 .. 0).find { accVal + it * 5.pow(nextPow) + sumPower5(nextPow - 1, 2) >= dec } ?: -2
        } else {
            (2 downTo 0).find { accVal + it * 5.pow(nextPow) + sumPower5(nextPow - 1, -2) <= dec } ?: 2
        }

        return loop(acc + MulPower(nextMul, nextPow))
    }

    return loop().joinToString(separator = "") { reverseSnafuCoding.getValue(it).toString() }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day25/input.txt")
        .readText()
        .trim()

    part1(input).also { println("P1: ${it}ms") }
}

private fun part1(input: String) = measureTimeMillis {
    input.lines()
        .fold(0L) { acc, snafu -> acc + snafuToDec(snafu) }
        .let(::decToSnafu)
        .also(::println)
}
