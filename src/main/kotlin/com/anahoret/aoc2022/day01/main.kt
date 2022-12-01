package com.anahoret.aoc2022.day01

import java.io.File

// https://adventofcode.com/2022/day/1
fun main() {
    val elfsData = File("src/main/kotlin/com/anahoret/aoc2022/day01/input1.txt")
        .readText()
        .split("\n\n")

    // Part 1
    elfsData
        .maxOfOrNull { it.trim().split("\n").map(String::toInt).sum() }
        .let(::println)

    // Part 2
    elfsData
        .map { it.trim().split("\n").map(String::toInt).sum() }
        .sortedDescending()
        .take(3)
        .sum()
        .let(::println)
}
