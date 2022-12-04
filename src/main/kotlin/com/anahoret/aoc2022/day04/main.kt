package com.anahoret.aoc2022.day04

import java.io.File

private fun parseRange(str: String): IntRange {
    val (min, max) = str.split("-").map(String::toInt)
    return IntRange(min, max)
}

private fun parsePair(str: String): Pair<IntRange, IntRange> {
    val (first, second) = str.split(",")
        .map(::parseRange)
    return first to second
}

private infix fun IntRange.overlaps(other: IntRange): Boolean {
    return first in other || last in other || other.first in this || other.last in this
}

private operator fun IntRange.contains(other: IntRange): Boolean {
    return other.first in this && other.last in this
}

fun main() {
    val sectionPairs = File("src/main/kotlin/com/anahoret/aoc2022/day04/input.txt")
        .readText()
        .split("\n")
        .filter(String::isNotEmpty)
        .map(::parsePair)

    // Part 1
    sectionPairs
        .count { (first, second) -> first in second || second in first }
        .let(::println)

    // Part 2
    sectionPairs
        .count { (first, second) ->  first overlaps second }
        .let(::println)
}
