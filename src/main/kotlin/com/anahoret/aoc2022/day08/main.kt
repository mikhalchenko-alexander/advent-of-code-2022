package com.anahoret.aoc2022.day08

import java.io.File

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day08/input.txt")
        .readText()
        .trim()

    val matrix = parse(input)

    // Part 1
    part1(matrix)

    // Part 2
    part2(matrix)
}

fun parse(input: String): List<List<Int>> {
    return input.split("\n").map { it.map(Char::digitToInt) }
}

private fun part1(matrix: List<List<Int>>) {
    matrix.indices.sumOf { rowIdx ->
        matrix[rowIdx].indices.count { colIdx -> isVisible(matrix, rowIdx, colIdx) }
    }.let(::println)
}

private fun part2(matrix: List<List<Int>>) {
    matrix.indices
        .maxOf { rowIdx -> matrix[rowIdx].indices.maxOf { colIdx -> scenicScore(matrix, rowIdx, colIdx) } }
        .let(::println)
}

fun isVisible(matrix: List<List<Int>>, rowIdx: Int, colIdx: Int): Boolean {
    fun shorterThanCurrentTree(i: Int) = i < matrix[rowIdx][colIdx]
    val row = matrix.row(rowIdx)
    val col = matrix.col(colIdx)
    val visibleTop = col.take(rowIdx).all(::shorterThanCurrentTree)
    val visibleBottom = col.drop(rowIdx + 1).all(::shorterThanCurrentTree)
    val visibleLeft = row.take(colIdx).all(::shorterThanCurrentTree)
    val visibleRight = row.drop(colIdx + 1).all(::shorterThanCurrentTree)
    return visibleTop || visibleBottom || visibleLeft || visibleRight
}

fun List<List<Int>>.row(rowIdx: Int): List<Int> = this[rowIdx]
fun List<List<Int>>.col(colIdx: Int): List<Int> = this.map { it[colIdx] }

fun scenicScore(matrix: List<List<Int>>, rowIdx: Int, colIdx: Int): Int {
    fun countVisibleTrees(list: List<Int>): Int {
        return list
            .indexOfFirst { it >= matrix[rowIdx][colIdx] }
            .inc()
            .takeIf { it > 0 }
            ?: list.size
    }

    val scoreTop = matrix.col(colIdx).take(rowIdx).reversed().let(::countVisibleTrees)
    val scoreBottom = matrix.col(colIdx).drop(rowIdx + 1).let(::countVisibleTrees)
    val scoreLeft = matrix.row(rowIdx).take(colIdx).reversed().let(::countVisibleTrees)
    val scoreRight = matrix.row(rowIdx).drop(colIdx + 1).let(::countVisibleTrees)
    return scoreTop * scoreBottom * scoreLeft * scoreRight
}
