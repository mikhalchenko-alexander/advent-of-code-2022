package com.anahoret.aoc2022.day06

import java.io.File

private fun allUnique(str: String): Boolean {
    return str.toSet().size == str.length
}

private fun firstUniqueIndex(str: String, uniqueLength: Int): Int {
    return str.windowed(size = uniqueLength)
        .indexOfFirst(::allUnique) + uniqueLength
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day06/input.txt")
        .readText()
        .trim()

    // Part 1
    println(firstUniqueIndex(input, 4))

    // Part 2
    println(firstUniqueIndex(input, 14))
}
