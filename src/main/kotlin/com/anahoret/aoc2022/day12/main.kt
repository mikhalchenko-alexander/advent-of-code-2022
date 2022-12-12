package com.anahoret.aoc2022.day12

import java.io.File
import java.util.PriorityQueue

data class Hill(val row: Int, val col: Int, val height: Int)

class Terrain(private val hills: List<List<Hill>>, val start: Hill, val end: Hill) {

    companion object {
        fun parse(str: String): Terrain {
            lateinit var start: Hill
            lateinit var end: Hill
            val hills = str.split("\n").mapIndexed { rowIdx, row ->
                row.mapIndexed { colIdx, char ->
                    val heightChar = if (char == 'S') 'a' else if (char == 'E') 'z' else char
                    val hill = Hill(rowIdx, colIdx, heightMap.getValue(heightChar))
                    if (char == 'S') start = hill
                    if (char == 'E') end = hill
                    hill
                }
            }
            return Terrain(hills, start, end)
        }
    }

    fun possibleNeighbours(hill: Hill): List<Hill> {
        val (row, col, height) = hill
        return listOf(
            row - 1 to col,
            row + 1 to col,
            row to col - 1,
            row to col + 1
        ).mapNotNull { (r, c) -> hills.getOrNull(r)?.getOrNull(c) }
            .filter { it.height <= height + 1 }
    }

    fun hillsOfHeight(height: Int): List<Hill> {
        return hills.flatten().filter { it.height == height }
    }
}

val heightMap = ('a'..'z').withIndex().associate { it.value to it.index + 1 }

fun main() {
    val hills = File("src/main/kotlin/com/anahoret/aoc2022/day12/input.txt")
        .readText()
        .trim()
        .let(Terrain.Companion::parse)

    // Part 1
    part1(hills)

    // Part 2
    part2(hills)
}

private fun part1(terrain: Terrain) {
    println(shortestPathLength(terrain, terrain.start, terrain.end))
}

private fun part2(terrain: Terrain) {
    terrain.hillsOfHeight(1)
        .mapNotNull { hill -> shortestPathLength(terrain, start = hill, end = terrain.end) }
        .min()
        .let(::println)
}

data class Path(private val hills: List<Hill>, val cost: Int) : Comparable<Path> {

    constructor(start: Hill) : this(listOf(start), 0)

    val end = hills.last()

    operator fun plus(hill: Hill): Path {
        return copy(hills = hills + hill, cost = cost + 1)
    }

    override fun compareTo(other: Path): Int {
        return cost.compareTo(other.cost)
    }

}

fun shortestPathLength(terrain: Terrain, start: Hill, end: Hill): Int? {
    val paths = PriorityQueue<Path>()
    paths += Path(start)

    val visited = mutableSetOf<Hill>()

    var path = paths.poll()
    while (path.end != end) {
        terrain.possibleNeighbours(path.end)
            .filterNot(visited::contains)
            .also(visited::addAll)
            .forEach { paths += path + it }
        if (paths.isEmpty()) break
        path = paths.poll()
    }

    if (path.end != end) return null

    return path.cost
}
