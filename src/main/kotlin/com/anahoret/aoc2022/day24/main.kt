package com.anahoret.aoc2022.day24

import com.anahoret.aoc2022.ManhattanDistanceAware
import java.io.File
import java.util.PriorityQueue
import kotlin.system.measureTimeMillis

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}

data class Blizzard(val row: Int, val col: Int, val direction: Direction)

operator fun List<Blizzard>.contains(position: Position): Boolean {
    return any { b -> b.row == position.row && b.col == position.col }
}

class Valley(private val width: Int, private val height: Int, private val blizzards: List<Blizzard>) {

    val start = Position(0, 1)
    val finish = Position(height - 1, width - 2)
    private val blizzardHashes = blizzards.map { it.row * width + it.col }.toSet()

    fun tick(): Valley {
        val nextBlizzards = blizzards.map { blizzard ->
            when (blizzard.direction) {
                Direction.LEFT -> if (blizzard.col > 1) blizzard.copy(col = blizzard.col - 1) else blizzard.copy(col = width - 2)
                Direction.RIGHT -> if (blizzard.col < width - 2) blizzard.copy(col = blizzard.col + 1) else blizzard.copy(
                    col = 1
                )

                Direction.UP -> if (blizzard.row > 1) blizzard.copy(row = blizzard.row - 1) else blizzard.copy(row = height - 2)
                Direction.DOWN -> if (blizzard.row < height - 2) blizzard.copy(row = blizzard.row + 1) else blizzard.copy(
                    row = 1
                )
            }
        }
        return Valley(width, height, nextBlizzards)
    }

    fun walkable(position: Position): Boolean {
        return position in this && position.row * width + position.col !in blizzardHashes
    }

    operator fun contains(position: Position): Boolean {
        return position == start || position == finish || (
                position.row > 0 &&
                        position.row < height - 1 &&
                        position.col > 0 &&
                        position.col < width - 1
                )
    }

}

fun parseValley(input: String): Valley {
    val lines = input.lines()
    val width = lines.first().length
    val height = lines.size

    val blizzards = lines.flatMapIndexed { rowIdx, row ->
        row.toList().mapIndexedNotNull { colIdx, col ->
            when (col) {
                '<' -> Blizzard(rowIdx, colIdx, Direction.LEFT)
                '>' -> Blizzard(rowIdx, colIdx, Direction.RIGHT)
                'v' -> Blizzard(rowIdx, colIdx, Direction.DOWN)
                '^' -> Blizzard(rowIdx, colIdx, Direction.UP)
                else -> null
            }
        }
    }
    return Valley(width, height, blizzards)
}

data class Position(val row: Int, val col: Int) : ManhattanDistanceAware {

    override val x: Int = col
    override val y: Int = row

    val neighbours by lazy {
        listOf(
            Position(row - 1, col),
            Position(row + 1, col),
            Position(row, col - 1),
            Position(row, col + 1)
        )
    }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day24/input.txt")
        .readText()
        .trim()

    val valley = parseValley(input)

    // Part 1
    part1(valley).also { println("P1: ${it}ms") }

    // Part 2
    part2(valley).also { println("P2: ${it}ms") }
}

private fun part1(initialValley: Valley) = measureTimeMillis {
    println(timeToTrip(initialValley, initialValley.start, initialValley.finish).first)
}

private fun part2(initialValley: Valley) = measureTimeMillis {
    val (timeToGoToFinish, valleyAtFinish) = timeToTrip(initialValley, initialValley.start, initialValley.finish)
    val (timeToGoBackToStart, valleyAtBackToStart) = timeToTrip(valleyAtFinish, initialValley.finish, initialValley.start)
    val (timeToGoBackToFinish, _) = timeToTrip(valleyAtBackToStart, initialValley.start, initialValley.finish)
    println(timeToGoToFinish + timeToGoBackToStart + timeToGoBackToFinish)
}

private fun timeToTrip(initialValley: Valley, start: Position, finish: Position): Pair<Int, Valley> {
    val valleySteps = mutableMapOf<Int, Valley>()
    valleySteps[0] = initialValley

    fun getValleyStep(step: Int): Valley {
        return valleySteps.getOrPut(step) { getValleyStep(step - 1).tick() }
    }

    var finishSteps = Int.MAX_VALUE

    val queue = PriorityQueue { p1: Pair<Position, Int>, p2: Pair<Position, Int> ->
        p1.first.manhattanDistance(finish).compareTo(p2.first.manhattanDistance(finish))
            .takeIf { it != 0 }
            ?: p1.second.compareTo(p2.second)
    }
    queue.add(start to 0)

    val observed = mutableSetOf<Pair<Position, Int>>()

    while (queue.isNotEmpty()) {
        val poll = queue.poll()
        val (position, step) = poll
        if (step >= finishSteps || poll in observed) continue
        observed.add(poll)

        if (position == finish) {
            finishSteps = step
            queue.removeIf { it.second >= finishSteps || it.second + it.first.manhattanDistance(finish) >= finishSteps }
        }

        val nextStep = step + 1

        if (nextStep < finishSteps) {
            val nextValley = getValleyStep(nextStep)
            (position.neighbours + position)
                .forEach {
                    val newPair = it to nextStep
                    if (
                        newPair !in observed &&
                        nextStep + it.manhattanDistance(finish) < finishSteps &&
                        nextValley.walkable(it)
                    ) {
                        queue.add(newPair)
                    }
                }
        }
    }

    return finishSteps to valleySteps.getValue(finishSteps)
}