package com.anahoret.aoc2022.day03

import java.io.File

fun main() {
    val rucksackData = File("src/main/kotlin/com/anahoret/aoc2022/day03/input.txt")
        .readText()
        .split("\n")
        .filter(String::isNotEmpty)

    val priorities = (('a'..'z').zip(1..26) + ('A'..'Z').zip(27..52))
        .associate { it.first to it.second }

    // Part 1

    rucksackData.sumOf { rucksack ->
        rucksack
            .chunked(rucksack.length / 2, CharSequence::toSet)
            .reduce(Set<Char>::intersect)
            .sumOf(priorities::getValue)
    }.let { println(it) }

    // Part 2

    rucksackData.chunked(3)
        .sumOf { groupOfThree ->
            groupOfThree
                .map(String::toSet)
                .reduce(Set<Char>::intersect)
                .sumOf(priorities::getValue)
        }.let { println(it) }
}
