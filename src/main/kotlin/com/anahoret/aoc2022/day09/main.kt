package com.anahoret.aoc2022.day09

import java.io.File
import kotlin.math.*

data class Point(val x: Int, val y: Int) {

    operator fun plus(move: Move): Point {
        return Point(x + move.dx, y + move.dy)
    }

    fun follow(target: Point): Point {
        val dx = target.x - x
        val dy = target.y - y

        return if (abs(dx) > 1 && abs(dy) > 0 || abs(dx) > 0 && abs(dy) > 1) this + Move(dx.sign, dy.sign)
        else if (abs(dx) > 1) this + Move(dx.sign, 0)
        else if (abs(dy) > 1) this + Move(0, dy.sign)
        else this
    }

}

data class Move(val dx: Int, val dy: Int) {

    companion object {
        private val moves = mapOf(
            "U" to Move(0, -1),
            "D" to Move(0, 1),
            "L" to Move(-1, 0),
            "R" to Move(1, 0)
        )

        fun parse(str: String): List<Move> {
            val (direction, steps) = str.split(" ")
            val move = moves.getValue(direction)
            return List(steps.toInt()) { move }
        }
    }
}

fun main() {
    val moves = File("src/main/kotlin/com/anahoret/aoc2022/day09/input.txt")
        .readText()
        .trim()
        .split("\n")
        .flatMap { Move.parse(it) }

    // Part 1
    part1(moves)

    // Part 2
    part2(moves)
}

private fun part1(moves: List<Move>) {
    println(trackTail(2, moves))
}

private fun part2(moves: List<Move>) {
    println(trackTail(10, moves))
}

private fun trackTail(length: Int, moves: List<Move>): Int {
    val rope = Array(length) { Point(0, 0) }
    val res = mutableSetOf<Point>()

    fun shiftRope(move: Move) {
        rope.indices.forEach { idx ->
            when (idx) {
                0 -> rope[idx] = rope[idx] + move
                else -> rope[idx] = rope[idx].follow(rope[idx - 1])
            }
        }
    }

    res.add(rope.last())
    moves.forEach { move ->
        shiftRope(move)
        res.add(rope.last())
    }

    return res.size
}
