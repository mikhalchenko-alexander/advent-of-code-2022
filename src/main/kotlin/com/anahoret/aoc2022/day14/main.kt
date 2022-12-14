package com.anahoret.aoc2022.day14

import java.io.File

data class Point(val x: Int, val y: Int) {
    fun moveDown(): Point {
        return Point(x, y + 1)
    }

    fun moveDownLeft(): Point {
        return Point(x - 1, y + 1)
    }

    fun moveDownRight(): Point {
        return Point(x + 1, y + 1)
    }

    companion object {
        fun parse(str: String): Point {
            val (x, y) = str.split(",").map(String::toInt)
            return Point(x, y)
        }
    }
}

class RockLine(start: Point, end: Point) {

    constructor(startEnd: Pair<Point, Point>) : this(startEnd.first, startEnd.second)

    companion object {
        fun parse(str: String): List<RockLine> {
            fun trimAndParse(s: String): Point = Point.parse(s.trim())
            return str.split("->")
                .map(::trimAndParse)
                .zipWithNext()
                .map(::RockLine)
        }
    }

    val points = range(start.y, end.y).flatMap { y ->
        range(start.x, end.x).map { x ->
            Point(x, y)
        }
    }

    private fun range(start: Int, end: Int): IntProgression {
        return if (start < end) start..end
        else start downTo end
    }

}

sealed interface DropSandResult
class Landed(val point: Point) : DropSandResult
object WentToVoid : DropSandResult

class CaveMap(occupied: Set<Point>, private val hasFloor: Boolean) {

    companion object {
        fun parse(str: String, hasFloor: Boolean): CaveMap {
            val lines = parseRockLines(str)
            val occupied = lines.flatMap(RockLine::points).toSet()
            return CaveMap(occupied, hasFloor)
        }

        private fun parseRockLines(str: String): List<RockLine> {
            return str
                .split("\n")
                .flatMap(RockLine::parse)
        }
    }

    private val occupied = occupied.toMutableSet()
    private val sandSpawn = Point(500, 0)
    private val floorLevel = occupied.maxOf { it.y } + 2
    private val maxOccupiedY = if (hasFloor) floorLevel else occupied.maxOf(Point::y)

    fun dropSand(): DropSandResult {
        return dropSandFrom(sandSpawn)
            .also { if (it is Landed) occupied.add(it.point) }
    }

    private fun dropSandFrom(point: Point): DropSandResult {
        val down = point.moveDown()
        val downLeft = point.moveDownLeft()
        val downRight = point.moveDownRight()
        return when {
            point in occupied -> WentToVoid
            isVoid(down) -> WentToVoid
            isAir(down) -> dropSandFrom(down)
            isVoid(downLeft) -> WentToVoid
            isAir(downLeft) -> dropSandFrom(downLeft)
            isVoid(downRight) -> WentToVoid
            isAir(downRight) -> dropSandFrom(downRight)
            else -> Landed(point)
        }
    }

    private fun isAir(point: Point): Boolean {
        return when {
            hasFloor -> point.y < floorLevel && !occupied.contains(point)
            else -> !occupied.contains(point)
        }
    }

    private fun isVoid(point: Point): Boolean {
        return !hasFloor && point.y > maxOccupiedY
    }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day14/input.txt")
        .readText()
        .trim()

    // Part 1
    part1(CaveMap.parse(input, hasFloor = false))

    // Part 2
    part2(CaveMap.parse(input, hasFloor = true))
}

private fun part1(map: CaveMap) {
    var rounds = 0
    while (map.dropSand() is Landed) { rounds++ }
    println(rounds)
}

private fun part2(map: CaveMap) {
    var rounds = 0
    while (map.dropSand() is Landed) { rounds++ }
    println(rounds)
}
