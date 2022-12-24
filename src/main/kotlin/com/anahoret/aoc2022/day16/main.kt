package com.anahoret.aoc2022.day16

import java.io.File
import kotlin.math.max
import kotlin.system.measureTimeMillis

data class Valve(val name: String, val flowRate: Long, var tunnels: List<String>)

fun parse(str: String): List<Valve> {
    val regex = "Valve (.+) has flow rate=(\\d+); tunnels* leads* to valves* (.+)".toRegex()
    return str.split("\n")
        .map { line ->
            val (_, name, rate, tunnels) = regex.find(line)!!.groupValues
            Valve(name, rate.toLong(), tunnels.split(",").map(String::trim))
        }
}

private fun findBestScore(
    valves: List<Valve>,
    totalTime: Int,
    distanceCalculator: DistanceCalculator,
    threshold: Long = -1
): Long {
    var maxRelease = 0L

    val totalReleaseOfAllValves = valves.sumOf(Valve::flowRate)

    fun loop(
        visited: List<Valve>,
        rest: List<Valve>,
        released: Long,
        timeLeft: Int = totalTime
    ) {
        val visitedReleasePerTick = visited.sumOf(Valve::flowRate)

        if (rest.isEmpty() || timeLeft < 3) {
            val totalReleased = released + timeLeft * visitedReleasePerTick
            maxRelease = max(maxRelease, totalReleased)
            return
        }

        val potentialRelease = released + 2 * visitedReleasePerTick + (timeLeft - 2) * totalReleaseOfAllValves

        if (potentialRelease <= maxRelease || potentialRelease <= threshold) {
            return
        }

        rest.forEach { rv ->
            val timeToMoveAndTurnOn = distanceCalculator.distanceBetween(visited.last(), rv) + 1
            if (timeLeft < timeToMoveAndTurnOn + 1) {
                val totalReleased = released + timeLeft * visitedReleasePerTick
                maxRelease = max(maxRelease, totalReleased)
                return@forEach
            }

            val releasedWhileMovingAndTurningOn = timeToMoveAndTurnOn * visitedReleasePerTick
            loop(
                visited = visited + rv,
                rest = rest.filter { it != rv },
                released = released + releasedWhileMovingAndTurningOn,
                timeLeft = timeLeft - timeToMoveAndTurnOn
            )
        }
    }

    val start = valves.find { it.name == "AA" }!!

    loop(listOf(start), valves.filter { it.flowRate > 0 }, 0)
    return maxRelease
}

class DistanceCalculator(valves: List<Valve>) {

    private val distanceCache = mutableMapOf<Pair<Valve, Valve>, Int>()
    private val valvesMap = valves.associateBy(Valve::name)
    private val tunnelMap = valves.associate { it.name to it.tunnels.map(valvesMap::getValue) }

    fun distanceBetween(v1: Valve, v2: Valve): Int {
        val cacheKey = if (v1.name < v2.name) v1 to v2 else v2 to v1
        val cached = distanceCache[cacheKey]
        if (cached != null) {
            return cached
        }

        val visited = mutableSetOf<Valve>()
        val queue = ArrayDeque<Pair<Valve, Int>>()
        queue.add(v1 to 0)

        while (queue.isNotEmpty()) {
            val v = queue.removeFirst()
            if (v.first == v2) {
                distanceCache[cacheKey] = v.second

                return v.second
            }
            visited.add(v.first)
            val tunnels = tunnelMap.getValue(v.first.name)
            tunnels.forEach { if (it !in visited) queue.addLast(it to v.second + 1) }
        }

        distanceCache[v1 to v2] = -1
        return -1
    }
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day16/input.txt")
        .readText()
        .trim()

    val valves = parse(input)

    val distanceCalculator = DistanceCalculator(valves)

    // Part 1
    part1(valves, distanceCalculator).also { println("P1: ${it}ms") }

    // Part 2
    part2(valves, distanceCalculator).also { println("P2: ${it}ms") }
}

private fun part1(valves: List<Valve>, distanceCalculator: DistanceCalculator) = measureTimeMillis {
    findBestScore(valves, totalTime = 30, distanceCalculator)
        .also { println(it) }
}

private fun part2(valves: List<Valve>, distanceCalculator: DistanceCalculator) = measureTimeMillis {
    fun <T> split(l: List<T>, acc: List<List<T>> = emptyList()): List<List<T>> {
        if (l.isEmpty()) return acc
        if (acc.isEmpty()) return split(l.drop(1), listOf(listOf(l.first())))
        return split(l.drop(1), acc + acc.map { ae ->
            ae + l.first()
        })
    }

    val start = valves.first { it.name == "AA" }
    val nonZeroValves = valves.filter { it.flowRate > 0 }
    val candidates = split(nonZeroValves)
        .map { left ->
            (listOf(start) + left) to (listOf(start) + nonZeroValves.filter { it !in left })
        }

    candidates.fold(0L) { maxRelease, (l, r) ->
        val release = if (l.size < r.size) {
            val ls = findBestScore(l, totalTime = 26, distanceCalculator)
            val rs = findBestScore(r, totalTime = 26, distanceCalculator, threshold = maxRelease - ls)
            ls + rs
        } else {
            val rs = findBestScore(r, totalTime = 26, distanceCalculator)
            val ls = findBestScore(l, totalTime = 26, distanceCalculator, threshold = maxRelease - rs)
            ls + rs
        }
        max(maxRelease, release)
    }.also(::println)
}
