package com.anahoret.aoc2022.day22

import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day22/input.txt")
        .readText()
        .trimEnd()

    // Part 1
    part1(input).also { println("P1: ${it}ms") }

    // Part 2
    part2(input).also { println("P2: ${it}ms") }
}

private fun part1(input: String) = measureTimeMillis {
    val (row, col, facing) = BoardSolver(input).solve()
    println(1000 * row + 4 * col + facing)
}

private fun part2(input: String) = measureTimeMillis {
    val (row, col, facing) = CubeSolver(input).solve()
    println(1000 * row + 4 * col + facing)
}
