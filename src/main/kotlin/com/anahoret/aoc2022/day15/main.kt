package com.anahoret.aoc2022.day15

import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Point(val x: Int, val y: Int) {
    fun manhattanDistanceTo(other: Point): Int {
        return abs(x - other.x) + abs(y - other.y)
    }
}

class Sensor(position: Point, val closestBeacon: Point) {

    val beaconDistance = position.manhattanDistanceTo(closestBeacon)
    val x = position.x
    val y = position.y

    companion object {
        private val regex = "Sensor at x=(.+), y=(.+): closest beacon is at x=(.+), y=(.+)".toRegex()
        fun parse(str: String): Sensor {
            val (_, sx, sy, bx, by) = regex.find(str)!!.groupValues
            return Sensor(Point(sx.toInt(), sy.toInt()), Point(bx.toInt(), by.toInt()))
        }
    }
}

fun parse(str: String): List<Sensor> {
    return str.split("\n")
        .map(Sensor::parse)
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day15/input.txt")
        .readText()
        .trim()
        .let(::parse)

    // Part 1
    part1(input)

    // Part 2
    part2(input)
}

fun IntRange.overlap(other: IntRange): IntRange {
    return if (first in other) first..min(last, other.last)
    else if (last in other) max(first, other.first)..last
    else IntRange.EMPTY
}

fun IntRange.contains(other: IntRange): Boolean {
    return other.first in this && other.last in this
}

fun IntRange.size() = if (isEmpty()) 0 else abs(this.last - this.first) + 1

private fun part1(sensors: List<Sensor>) {
    val searchRow = 2000000
    sensors.mapNotNull { findExcluded(it, searchRow) }
        .let(::merge)
        .sumOf(IntRange::size)
        .let(::println)
}

private fun part2(sensors: List<Sensor>) {
    val searchRange = 0..4000000
    val beaconRanges = sensors.map(Sensor::closestBeacon)
        .groupBy(Point::y)
        .mapValues { (_, v) -> v.map { it.x..it.x } }
    searchRange.asSequence()
        .mapNotNull { y ->
            val res = sensors
                .mapNotNull { sensor -> findExcluded(sensor, y) }
                .let { excludedRanges -> beaconRanges[y]?.let { excludedRanges + it } ?: excludedRanges }
                .let(::merge)
                .takeIf { it.size > 1 }
                ?.let { y to it.first().last + 1 }
            res
        }.first()
        .let { (y, x) -> x.toLong() * 4000000 + y }
        .let(::println)
}

fun merge(ranges: List<IntRange>): List<IntRange> {
    val newRanges = ranges
        .sortedBy(IntRange::first)
        .fold(mutableListOf<IntRange>()) { acc, range ->
            if (acc.isEmpty()) acc.add(range)
            else {
                val last = acc.last()
                if (range.contains(last)) acc[acc.lastIndex] = range
                else if (!last.contains(range)) {
                    val overlap = range.overlap(last)
                    val notConnected = overlap.isEmpty() && range.first > last.last + 1
                    if (notConnected) acc.add(range)
                    else acc[acc.lastIndex] = last.first..range.last
                }
            }
            acc
        }
    return newRanges
}

fun findExcluded(sensor: Sensor, row: Int): IntRange? {
    if (abs(sensor.y - row) > sensor.beaconDistance) return null

    val minX = sensor.x - sensor.beaconDistance + abs(sensor.y - row)
    val halfWidth = abs(sensor.x - minX)
    return ((sensor.x - halfWidth)..(sensor.x + halfWidth))
        .let {
            when (sensor.closestBeacon.x) {
                it.first -> it.first.inc()..it.last
                it.last -> it.first..it.last.dec()
                else -> it
            }
        }
}
