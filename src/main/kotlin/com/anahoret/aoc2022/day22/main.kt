package com.anahoret.aoc2022.day22

import com.anahoret.aoc2022.calculateEndIndex
import java.io.File
import java.lang.RuntimeException
import kotlin.system.measureTimeMillis

sealed class Tile(val row: Int, val col: Int) {
    companion object {
        fun parse(row: Int, col: Int, char: Char): Tile? {
            return when (char) {
                '.' -> OpenTile(row, col)
                '#' -> SolidWall(row, col)
                else -> null
            }
        }
    }
}

class OpenTile(row: Int, col: Int) : Tile(row, col) {
    override fun toString(): String = "."
}
class SolidWall(row: Int, col: Int) : Tile(row, col) {
    override fun toString(): String = "#"
}

class TileList(val minIndex: Int, val maxIndex: Int, val rockPositions: List<Int>) {
    fun stoneAfter(col: Int): Int? {
        return rockPositions.firstOrNull { it > col } ?: rockPositions.firstOrNull()
    }

    fun stoneBefore(col: Int): Int? {
        return rockPositions.lastOrNull { it < col } ?: rockPositions.lastOrNull()
    }
}

class Board(tiles: List<Tile>) {
    val rows = groupToList(tiles, Tile::row, Tile::col)
    val cols = groupToList(tiles, Tile::col, Tile::row)

    private fun groupToList(tiles: List<Tile>, listSelector: (Tile) -> Int, elementSelector: (Tile) -> Int): Map<Int, TileList> {
        return tiles.groupBy(listSelector)
            .mapValues { (_, tiles) ->
                val minIndex = tiles.minOf(elementSelector)
                val maxIndex = tiles.maxOf(elementSelector)
                val rockPositions = tiles.filterIsInstance<SolidWall>().map(elementSelector)
                TileList(minIndex, maxIndex, rockPositions)
            }
    }

    override fun toString(): String {
        val minRow = 1
        val maxRow = cols.maxOf { it.value.maxIndex }

        val minCol = 1
        val maxCol = rows.maxOf { it.value.maxIndex }

        return (minRow..maxRow).joinToString(separator = "\n") { row ->
            (minCol..maxCol).joinToString(separator = "") { col ->
                val r = rows.getValue(row)
                if (col in r.minIndex..r.maxIndex && col !in r.rockPositions) "."
                else if (col in r.minIndex..r.maxIndex && col in r.rockPositions) "#"
                else if (col < r.minIndex) " "
                else ""
            }
        }
    }
}

enum class Direction(val facing: Int) {
    LEFT(2), RIGHT(0), UP(3), DOWN(1);

    fun rotate(direction: Direction): Direction {
        return when (direction) {
            LEFT -> rotateLeft()
            RIGHT -> rotateRight()
            else -> throw IllegalArgumentException("Cannot rotate $direction")
        }
    }

    override fun toString(): String {
        return "$name($facing)"
    }

    private fun rotateRight(): Direction {
        return when (this) {
            LEFT -> UP
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
        }
    }

    private fun rotateLeft(): Direction {
        return when (this) {
            LEFT -> DOWN
            DOWN -> RIGHT
            RIGHT -> UP
            UP -> LEFT
        }
    }

    companion object {
        private val dirMap = mapOf(
            'L' to LEFT,
            'R' to RIGHT
        )
        fun fromChar(c: Char): Direction {
            return dirMap.getValue(c)
        }
    }
}

sealed interface Action
class Movement(val steps: Int): Action {
    override fun toString(): String {
        return "Move $steps"
    }
}
class Rotation(val direction: Direction): Action {
    override fun toString(): String {
        return "Rotate $direction"
    }
}

class Path(val actions: List<Action>) {
    override fun toString(): String {
        return actions.joinToString(separator = "") {
            when (it) {
                is Movement -> it.steps.toString()
                is Rotation -> it.direction.name.first().toString()
            }
        }
    }
}

fun parseBoard(str: String): Board {
    val tiles = str.split("\n").mapIndexed { rowIdx, line ->
        line.mapIndexedNotNull { colIdx, char -> Tile.parse(rowIdx + 1, colIdx + 1, char) }
    }.flatten()
    return Board(tiles)
}

fun parsePath(str: String): Path {

    fun parseActions(str: String, acc: List<Action>): List<Action> {
        if (str.isEmpty()) return acc

        return if (str.first().isDigit()) {
            val steps = str.takeWhile(Char::isDigit).toInt()
            val movement = Movement(steps)
            parseActions(str.dropWhile(Char::isDigit), acc + movement)
        } else {
            val direction = Direction.fromChar(str.first())
            val rotation = Rotation(direction)
            parseActions(str.drop(1), acc + rotation)
        }
    }

    val actions = parseActions(str, emptyList())

    return Path(actions)
}

fun parseInput(str: String): Pair<Board, Path> {
    val (boardStr, pathStr) = str.split("\n\n")
    return parseBoard(boardStr) to parsePath(pathStr)
}

data class Position(var row: Int, var col: Int, var direction: Direction)

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day22/input.txt")
        .readText()
        .trimEnd()

    val (board, path) = parseInput(input)


    // Part 1
    part1(board, path).also { println("P1: ${it}ms") }

    // Part 2
    part2().also { println("P2: ${it}ms") }
}

fun Position.move(board: Board, action: Action) {

    fun loop(tileList: TileList, pos: Int, steps: Int): Int {
        val endIndex = calculateEndIndex(pos - tileList.minIndex, steps, tileList.maxIndex - tileList.minIndex + 1)
        return endIndex + tileList.minIndex
    }

    fun move(steps: Int, tileList: TileList, pos: Int): Int {
        val last = tileList.maxIndex
        val first = tileList.minIndex

        fun movePositive(stone: Int): Int {
            return when {
                stone > pos && stone > pos + steps -> pos + steps
                stone > pos && stone <= pos + steps -> stone - 1

                stone < pos && pos + steps <= last -> pos + steps
                stone < pos && pos + steps > last && first + steps - (last - pos) < stone -> loop(tileList, pos, steps)
                stone < pos && pos + steps > last && first + steps - (last - pos) >= stone && stone == first -> last
                stone < pos && pos + steps > last && first + steps - (last - pos) >= stone && stone > first -> stone - 1

                else -> throw RuntimeException("Unhandled case.")
            }
        }

        fun moveNegative(stone: Int): Int {
            return when {
                stone < pos && stone < pos + steps -> pos + steps
                stone < pos && stone >= pos + steps -> stone + 1

                stone > pos && pos + steps >= first -> pos + steps
                stone > pos && pos + steps < first && last + steps + (pos - first) > stone -> loop(tileList, pos, steps)
                stone > pos && pos + steps < first && last + steps + (pos - first) <= stone  && stone == last -> first
                stone > pos && pos + steps < first && last + steps + (pos - first) <= stone  && stone < last -> stone + 1

                else -> throw RuntimeException("Unhandled case.")
            }
        }

        val stone = if (steps > 0) tileList.stoneAfter(pos) else tileList.stoneBefore(pos)
        return when {
            stone == null -> loop(tileList, pos, steps)
            steps > 0 -> movePositive(stone)
            else -> moveNegative(stone)
        }
    }

    fun moveHorizontally(steps: Int) {
        val currentRow = board.rows.getValue(row)
        col = move(steps, currentRow, col)
    }

    fun moveVertically(steps: Int) {
        val currentCol = board.cols.getValue(col)
        row = move(steps, currentCol, row)
    }

    when (action) {
        is Movement -> when(direction) {
            Direction.LEFT -> moveHorizontally(-action.steps)
            Direction.RIGHT -> moveHorizontally(action.steps)
            Direction.UP -> moveVertically(-action.steps)
            Direction.DOWN -> moveVertically(action.steps)
        }
        is Rotation -> direction = direction.rotate(action.direction)
    }
}

private fun part1(board: Board, path: Path) = measureTimeMillis {
    val startRow = 1
    val startCol = board.rows.getValue(startRow).minIndex
    val startDirection = Direction.RIGHT

    val currentPos = Position(startRow, startCol, startDirection)
    path.actions.forEach {
        currentPos.move(board, it)
    }

    println(1000 * currentPos.row + 4 * currentPos.col + currentPos.direction.facing)
}

private fun part2() = measureTimeMillis {
}
