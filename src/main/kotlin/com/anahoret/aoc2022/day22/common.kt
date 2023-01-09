package com.anahoret.aoc2022.day22

import com.anahoret.aoc2022.ManhattanDistanceAware

sealed class Tile(val row: Int, val col: Int) : ManhattanDistanceAware {

    override val x = col
    override val y = row

    companion object {
        fun parse(row: Int, col: Int, char: Char): Tile? {
            return when (char) {
                '.' -> OpenTile(row, col)
                '#' -> SolidWall(row, col)
                else -> null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tile) return false

        if (row != other.row) return false
        if (col != other.col) return false

        return true
    }

    override fun hashCode(): Int {
        var result = row
        result = 31 * result + col
        return result
    }

}

class OpenTile(row: Int, col: Int) : Tile(row, col) {
    override fun toString(): String = "$col $row"
}

class SolidWall(row: Int, col: Int) : Tile(row, col) {
    override fun toString(): String = "$col $row"
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
class Movement(val steps: Int) : Action {
    override fun toString(): String {
        return "Move $steps"
    }
}

class Rotation(val direction: Direction) : Action {
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

fun parseTiles(str: String) = str.split("\n").mapIndexed { rowIdx, line ->
    line.mapIndexedNotNull { colIdx, char -> Tile.parse(rowIdx + 1, colIdx + 1, char) }
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
