package com.anahoret.aoc2022.day17

import java.io.File
import kotlin.math.max
import kotlin.system.measureTimeMillis

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day17/input.txt")
        .readText()
        .trim()

    // Part 1
    part1(WindIterator(input)).also { println("P1: ${it}ms") }

    // Part 2
    part2(WindIterator(input)).also { println("P2: ${it}ms") }
}

private fun part1(windIterator: WindIterator) = measureTimeMillis {
    simulate(2022, windIterator)
        .also(::println)
}

private fun part2(windIterator: WindIterator) = measureTimeMillis {
    simulate(1_000_000_000_000L, windIterator)
        .also(::println)
}

data class Rock(val points: List<Point>, val x: Long, val y: Long) {

    fun all(predicate: (Long, Long) -> Boolean): Boolean {
        return points.all { p -> predicate(x + p.x, y + p.y) }
    }

    fun resolvedPoints(): List<Point> {
        return points.map { p -> Point(x + p.x, y + p.y) }
    }

}

const val CHAMBER_WIDTH = 7L
val CHAMBER_WIDTH_RANGE = 0 until CHAMBER_WIDTH

data class Point(val x: Long, val y: Long) {
    val hash = y * CHAMBER_WIDTH + x
}

enum class ShapeType {
    STICK_HORIZONTAL,
    STICK_VERTICAL,
    CROSS,
    CORNER,
    SQUARE;

    companion object {
        val cornerPoints = listOf(
            Point(0, 0),
            Point(1, 0),
            Point(2, 0),
            Point(2, 1),
            Point(2, 2),
        )

        val squarePoints = listOf(
            Point(0, 0),
            Point(1, 0),
            Point(0, 1),
            Point(1, 1),
        )

        val stickVerticalPoints = listOf(
            Point(0, 0),
            Point(0, 1),
            Point(0, 2),
            Point(0, 3),
        )

        val stickHorizontalPoints = listOf(
            Point(0, 0),
            Point(1, 0),
            Point(2, 0),
            Point(3, 0),
        )

        val crossPoints = listOf(
            Point(1, 0),
            Point(0, 1),
            Point(1, 1),
            Point(2, 1),
            Point(1, 2),
        )


    }

    fun spawn(height: Long): Rock {
        return when (this) {
            STICK_HORIZONTAL -> spawnStickHorizontal(height)
            STICK_VERTICAL -> spawnStickVertical(height)
            CROSS -> spawnCross(height)
            CORNER -> spawnCorner(height)
            SQUARE -> spawnSquare(height)
        }
    }

    private fun spawnCorner(height: Long): Rock {
        return Rock(
            points = cornerPoints,
            x = 2,
            y = height
        )
    }

    private fun spawnSquare(height: Long): Rock {
        return Rock(
            points = squarePoints,
            x = 2,
            y = height
        )
    }

    private fun spawnCross(height: Long): Rock {
        return Rock(
            points = crossPoints,
            x = 2,
            y = height
        )
    }

    private fun spawnStickHorizontal(height: Long): Rock {
        return Rock(
            points = stickHorizontalPoints,
            x = 2,
            y = height
        )
    }

    private fun spawnStickVertical(height: Long): Rock {
        return Rock(
            points = stickVerticalPoints,
            x = 2,
            y = height
        )
    }
}

enum class Direction {
    LEFT,
    RIGHT
}

abstract class LoopIterator<T>(private val elements: List<T>) : Iterator<T> {
    private var idx = 0
    private val lastIndex = elements.size - 1

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): T {
        val result = elements[idx++]
        if (idx > lastIndex) idx = 0
        return result
    }
}

class FigureIterator : LoopIterator<ShapeType>(
    listOf(
        ShapeType.STICK_HORIZONTAL,
        ShapeType.CROSS,
        ShapeType.CORNER,
        ShapeType.STICK_VERTICAL,
        ShapeType.SQUARE
    )
)

class WindIterator(str: String) : LoopIterator<Direction>(parse(str)) {
    companion object {
        fun parse(str: String): List<Direction> {
            return str.map {
                when (it) {
                    '<' -> Direction.LEFT
                    else -> Direction.RIGHT
                }
            }
        }
    }
}

private fun Set<Point>.hashes(): Set<Long> {
    return mapTo(mutableSetOf(), Point::hash)
}

fun Rock.moveLeft(restRocks: Set<Point>): Rock {
    val hashes = restRocks.hashes()
    val canMoveLeft = all { px, py ->
        val dx = px - 1
        val dxHash = py * CHAMBER_WIDTH + dx
        dx in CHAMBER_WIDTH_RANGE && dxHash !in hashes
    }
    if (!canMoveLeft) return this

    return copy(x = x - 1)
}

fun Rock.moveRight(restRocks: Set<Point>): Rock {
    val hashes = restRocks.hashes()
    val canMoveLeft = all { px, py ->
        val dx = px + 1
        val dxHash = py * CHAMBER_WIDTH + dx
        dx in CHAMBER_WIDTH_RANGE && dxHash !in hashes
    }
    if (!canMoveLeft) return this

    return copy(x = x + 1)
}

fun Rock.moveDown(restRocks: Set<Point>): Rock {
    val hashes = restRocks.hashes()
    val canMoveLeft = all { px, py ->
        val dy = py - 1
        val dyHash = dy * CHAMBER_WIDTH + px
        dy >= 0 && dyHash !in hashes
    }
    if (!canMoveLeft) return this

    return copy(y = y - 1)
}

private operator fun Pair<Long, Long>.minus(other: Pair<Long, Long>): Pair<Long, Long> {
    return first - other.first to second - other.second
}

private fun simulate(steps: Long, windIterator: WindIterator): Long {
    val figureIterator = FigureIterator()

    val restRocks = mutableSetOf<Point>()

    var maxHeight = 0L
    var rocksSpawned = 0L

    fun cleanupChamber() {
        val checkQueue = mutableSetOf(Point(0, maxHeight + 1))
        val visited = mutableSetOf<Point>()
        val reachable = mutableSetOf<Point>()

        while (checkQueue.isNotEmpty()) {
            checkQueue
                .onEach(visited::add)
                .toSet()
                .onEach { point ->
                    (point.x.dec()..point.x.inc()).flatMap { x ->
                        (point.y.dec()..point.y).map { y -> Point(x, y) }
                    }.filter { p ->
                        p.y >= 0 && p.x in CHAMBER_WIDTH_RANGE && p !in visited
                    }.forEach { p ->
                        if (p in restRocks) reachable.add(p)
                        else checkQueue.add(p)
                    }
                    checkQueue.remove(point)
                }
        }

        restRocks.retainAll(reachable)
    }

    val snapshots = mutableMapOf<Set<Point>, MutableList<Pair<Long, Long>>>()
    var fastForwarded = false

    fun findPattern(): Pair<Pair<Long, Long>, Pair<Long, Long>>? {
        return snapshots
            .filter { it.value.size > 2 }
            .takeIf { it.isNotEmpty() }
            ?.entries
            ?.find { (_, value) ->
                value
                    .zipWithNext()
                    .map { it.second - it.first }
                    .zipWithNext()
                    .all { it.first == it.second }
            }?.value
            ?.let { (first, second) -> first to second }
    }

    fun fastForward(pattern: Pair<Pair<Long, Long>, Pair<Long, Long>>) {
        val (first, second) = pattern
        val (dRocksSpawned, dMaxHeight) = second - first
        val iterationsToSkip = (steps - first.first) / dRocksSpawned
        val oldMaxHeight = maxHeight
        rocksSpawned = first.first + iterationsToSkip * dRocksSpawned
        maxHeight = first.second + iterationsToSkip * dMaxHeight
        val fastForwardedMaxY = maxHeight - oldMaxHeight
        val newRestRocks = restRocks.map { it.copy(y = it.y + fastForwardedMaxY) }
        restRocks.clear()
        restRocks.addAll(newRestRocks)
        fastForwarded = true
    }


    while (rocksSpawned < steps) {
        cleanupChamber()

        if (!fastForwarded) {
            val minY = restRocks.minOfOrNull(Point::y) ?: 0
            val snapshot = restRocks.map { it.copy(y = it.y - minY) }.toSet()
            snapshots.getOrPut(snapshot) { mutableListOf() }.add(rocksSpawned to maxHeight)
            findPattern()?.let(::fastForward)
        }

        var currentRock = figureIterator.next().spawn(maxHeight + 3)
        rocksSpawned++

        while (true) {
            val movedSide = when (windIterator.next()) {
                Direction.LEFT -> currentRock.moveLeft(restRocks)
                Direction.RIGHT -> currentRock.moveRight(restRocks)
            }

            val movedDown = movedSide.moveDown(restRocks)

            if (movedSide != movedDown) {
                currentRock = movedDown
            } else {
                val rockPoints = movedSide.resolvedPoints()
                restRocks.addAll(rockPoints)
                maxHeight = max(maxHeight, restRocks.maxOf(Point::y) + 1)
                break
            }
        }
    }

    return maxHeight
}
