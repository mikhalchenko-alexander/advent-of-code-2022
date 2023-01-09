package com.anahoret.aoc2022.day23

import java.io.File
import kotlin.system.measureTimeMillis

data class Elf(var row: Int, var col: Int) {

    operator fun plus(point: Point): Point {
        return Point(row + point.row, col + point.col)
    }

}
data class Point(val row: Int, val col: Int)

fun parseElves(input: String): List<Elf> {
    return input.split("\n").flatMapIndexed { rowIdx, row ->
        row.mapIndexedNotNull { colIdx, col ->
            if (col == '#') Elf(rowIdx, colIdx) else null
        }
    }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day23/input.txt")
        .readText()
        .trim()

    // Part 1
    part1(input).also { println("P1: ${it}ms") }

    // Part 2
    part2(input).also { println("P2: ${it}ms") }
}

fun Map<Int, Map<Int, Elf>>.anyAt(row: Int, col: Int): Boolean {
    return get(row)?.get(col) != null
}

private val checkOrder = listOf(
    listOf(Point(-1, -1), Point(-1, 0), Point(-1, 1)),
    listOf(Point(1, -1), Point(1, 0), Point(1, 1)),
    listOf(Point(-1, -1), Point(0, -1), Point(1, -1)),
    listOf(Point(-1, 1), Point(0, 1), Point(1, 1)),
)

private fun part1(input: String) = measureTimeMillis {
    val elves = parseElves(input)
    val proposes = mutableMapOf<Point, MutableList<Elf>>()
    var checkStart = 0

    repeat(10) {
        tick(elves, proposes, checkStart)

        proposes.filter { it.value.size == 1 }
            .forEach { (propose, elves) ->
                elves.forEach { it.row = propose.row; it.col = propose.col }
            }

        checkStart++
        if (checkStart == checkOrder.size) checkStart = 0
    }

    val minRow = elves.minOf { it.row }
    val maxRow = elves.maxOf { it.row }
    val minCol = elves.minOf { it.col }
    val maxCol = elves.maxOf { it.col }

    val area = (maxRow - minRow + 1) * (maxCol - minCol + 1)
    val emptyGround = area - elves.size

    println(emptyGround)
}

private fun part2(input: String) = measureTimeMillis {
    val elves = parseElves(input)
    val proposes = mutableMapOf<Point, MutableList<Elf>>()
    var checkStart = 0
    var round = 1

    while(true) {
        tick(elves, proposes, checkStart)

        var noElfMoved = true
        proposes.forEach { (propose, elves) ->
            if (elves.size == 1) {
                noElfMoved = false
                elves.forEach { it.row = propose.row; it.col = propose.col }
            }
        }

        if (noElfMoved) break

        checkStart++
        if (checkStart == checkOrder.size) checkStart = 0

        round++
    }

    println(round)
}

private fun tick(elves: List<Elf>, proposes: MutableMap<Point, MutableList<Elf>>, checkStart: Int) {
    val elvesMap = elves
        .groupBy(Elf::row)
        .mapValues { (_, value) -> value.associateBy(Elf::col) }
    proposes.forEach { it.value.clear() }
    elves.forEach { elf ->
        var matches = 0
        var firstMatch: Point? = null
        for (check in checkOrder.indices) {
            val checkSideIdx = (checkStart + check) % checkOrder.size
            val checkSide = checkOrder[checkSideIdx]
            if (checkSide.none { shift -> elvesMap.anyAt(elf.row + shift.row, elf.col + shift.col) }) {
                matches++
                if (firstMatch == null) firstMatch = elf + checkSide[1]
            }
        }
        if (matches < 4 && firstMatch != null) {
            proposes.getOrPut(firstMatch) { mutableListOf() }
                .add(elf)
        }
    }
}
