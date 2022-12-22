package com.anahoret.aoc2022.day18

import java.io.File
import kotlin.system.measureTimeMillis

data class Point(val x: Long, val y: Long, val z: Long) {
    fun neighbours(): List<Point> {
        return listOf(
            copy(x = x - 1),
            copy(x = x + 1),

            copy(y = y - 1),
            copy(y = y + 1),

            copy(z = z - 1),
            copy(z = z + 1),
        )
    }

    fun outside(boundingBox: BoundingBox): Boolean {
        return x !in boundingBox.xRange || y !in boundingBox.yRange || z !in boundingBox.zRange
    }
}

class BoundingBox(points: List<Point>) {
    val xRange = points.minOf { it.x }..points.maxOf { it.x }
    val yRange = points.minOf { it.y }..points.maxOf { it.y }
    val zRange = points.minOf { it.z }..points.maxOf { it.z }
}

fun parsePoints(str: String): List<Point> {
    return str.split("\n")
        .map { it.split(",").map(String::toLong) }
        .map { (x, y, z) -> Point(x, y, z) }
}

fun main() {
    val points = File("src/main/kotlin/com/anahoret/aoc2022/day18/input.txt")
        .readText()
        .trim()
        .let(::parsePoints)

    // Part 1
    part1(points).also { println("P1: ${it}ms") }

    // Part 2
    part2(points).also { println("P2: ${it}ms") }
}

private fun part1(points: List<Point>) = measureTimeMillis {
    calculateSurface(points)
        .also { println(it) }
}

private fun calculateSurface(
    points: List<Point>,
): Int {
    val lava = points.toSet()
    return points.sumOf { p ->
        6 - p.neighbours().count { it in lava }
    }
}

private fun part2(points: List<Point>) = measureTimeMillis {
    val boundingBox = BoundingBox(points)
    val lava = points.toSet()
    val neighbours = points.flatMap(Point::neighbours)

    val surface = calculateSurface(points)

    val internalCandidates = neighbours
        .filterNot { it in lava || it.outside(boundingBox) }
        .toMutableSet()

    fun growBulb(start: Point): Pair<Boolean, Set<Point>> {
        val acc = mutableSetOf(start)
        val visited = mutableSetOf<Point>()
        val queue = ArrayDeque<Point>()
        queue.add(start)
        var isAirPocket = true

        while (queue.isNotEmpty()) {
            val point = queue.removeFirst()
            visited.add(point)
            acc.add(point)

            val airNeighbours = point.neighbours()
                .filterNot { it in lava || it in visited }

            if (isAirPocket && (point.outside(boundingBox) || airNeighbours.any { it.outside(boundingBox) })) {
                isAirPocket = false
            }
            val airNeighboursInsideBorderBox = airNeighbours.filterNot { it.outside(boundingBox) }
            visited.addAll(airNeighboursInsideBorderBox)
            queue.addAll(airNeighboursInsideBorderBox)
        }

        return isAirPocket to acc
    }

    val airPocketPoints = mutableSetOf<Point>()

    while (internalCandidates.isNotEmpty()) {
        val candidate = internalCandidates.first()
        val (isAirPocket, bulb) = growBulb(candidate)
        internalCandidates.removeAll(bulb)
        if (isAirPocket) {
            airPocketPoints.addAll(bulb)
        }
    }

    val excludedSurface = airPocketPoints
        .flatMap(Point::neighbours)
        .count { it in lava }

    println(surface - excludedSurface)
}
