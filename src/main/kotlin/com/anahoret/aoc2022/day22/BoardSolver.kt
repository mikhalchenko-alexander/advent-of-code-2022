package com.anahoret.aoc2022.day22

import com.anahoret.aoc2022.calculateEndIndex
import java.lang.RuntimeException

class BoardSolver(private val input: String) {

    fun solve(): List<Int> {
        val (board, path) = parseInputToBoard(input)

        val startRow = 1
        val startCol = board.rows.getValue(startRow).minIndex
        val startDirection = Direction.RIGHT

        val currentPos = Position(startRow, startCol, startDirection)
        path.actions.forEach {
            currentPos.move(board, it)
        }
        return listOf(currentPos.row, currentPos.col, currentPos.direction.facing)
    }

    private fun parseInputToBoard(str: String): Pair<Board, Path> {
        val (boardStr, pathStr) = str.split("\n\n")
        return Board.parse(boardStr) to parsePath(pathStr)
    }

}

private data class Position(var row: Int, var col: Int, var direction: Direction)

private class TileList(val minIndex: Int, val maxIndex: Int, val rockPositions: List<Int>) {
    fun stoneAfter(col: Int): Int? {
        return rockPositions.firstOrNull { it > col } ?: rockPositions.firstOrNull()
    }

    fun stoneBefore(col: Int): Int? {
        return rockPositions.lastOrNull { it < col } ?: rockPositions.lastOrNull()
    }
}

private class Board(tiles: List<Tile>) {

    companion object {
        fun parse(str: String): Board {
            val tiles = parseTiles(str).flatten()
            return Board(tiles)
        }
    }

    val rows = groupToList(tiles, Tile::row, Tile::col)
    val cols = groupToList(tiles, Tile::col, Tile::row)

    private fun groupToList(
        tiles: List<Tile>,
        listSelector: (Tile) -> Int,
        elementSelector: (Tile) -> Int
    ): Map<Int, TileList> {
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

private fun Position.move(board: Board, action: Action) {

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
                stone > pos && pos + steps < first && last + steps + (pos - first) <= stone && stone == last -> first
                stone > pos && pos + steps < first && last + steps + (pos - first) <= stone && stone < last -> stone + 1

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
        is Movement -> when (direction) {
            Direction.LEFT -> moveHorizontally(-action.steps)
            Direction.RIGHT -> moveHorizontally(action.steps)
            Direction.UP -> moveVertically(-action.steps)
            Direction.DOWN -> moveVertically(action.steps)
        }

        is Rotation -> direction = direction.rotate(action.direction)
    }
}
