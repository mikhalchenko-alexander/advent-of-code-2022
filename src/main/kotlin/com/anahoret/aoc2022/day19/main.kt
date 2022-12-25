package com.anahoret.aoc2022.day19

import java.io.File
import kotlin.math.max
import kotlin.system.measureTimeMillis

typealias ResourcePile = Map<Resource, Int>

enum class Resource(val priority: Int) {
    ORE(0),
    CLAY(1),
    OBSIDIAN(2),
    GEODE(3)
}

class Blueprint(val id: Int, private val robotCosts: Map<Resource, ResourcePile>) {
    companion object {
        private val blueprintRegex =
            "Blueprint (.*): Each ore robot costs (.*) ore. Each clay robot costs (.*) ore. Each obsidian robot costs (.*) ore and (.*) clay. Each geode robot costs (.*) ore and (.*) obsidian.".toRegex()

        fun parse(str: String): Blueprint {
            val groups = blueprintRegex.find(str)!!.groupValues.mapNotNull(String::toIntOrNull)
            val id = groups[0]
            val oreRobotCost = mapOf(Resource.ORE to groups[1])
            val clayRobotCost = mapOf(Resource.ORE to groups[2])
            val obsidianRobotCost = mapOf(Resource.ORE to groups[3], Resource.CLAY to groups[4])
            val geodeRobotCost = mapOf(Resource.ORE to groups[5], Resource.OBSIDIAN to groups[6])
            return Blueprint(
                id,
                mapOf(
                    Resource.ORE to oreRobotCost,
                    Resource.CLAY to clayRobotCost,
                    Resource.OBSIDIAN to obsidianRobotCost,
                    Resource.GEODE to geodeRobotCost
                )
            )
        }
    }

    override fun toString(): String {
        return "Blueprint $id: Each ore robot costs ${robotCosts[Resource.ORE]!![Resource.ORE]} ore. Each clay robot costs ${robotCosts[Resource.CLAY]!![Resource.ORE]} ore. Each obsidian robot costs ${robotCosts[Resource.OBSIDIAN]!![Resource.ORE]} ore and ${robotCosts[Resource.OBSIDIAN]!![Resource.CLAY]} clay. Each geode robot costs ${robotCosts[Resource.GEODE]!![Resource.ORE]} ore and ${robotCosts[Resource.GEODE]!![Resource.OBSIDIAN]} obsidian."
    }

    fun getCost(resource: Resource): ResourcePile {
        return robotCosts.getValue(resource)
    }

}

fun parseBlueprints(str: String): List<Blueprint> {
    return str.split("\n").map(Blueprint.Companion::parse)
}

fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day19/input.txt")
        .readText()
        .trim()

    val blueprints = parseBlueprints(input)

    // Part 1
    part1(blueprints).also { println("P1: ${it}ms") }

    // Part 2
    part2(blueprints).also { println("P2: ${it}ms") }
}

private fun part1(blueprints: List<Blueprint>) = measureTimeMillis {
    blueprints.sumOf { it.id * evaluate(it, 24) }
        .also(::println)
}

private fun part2(blueprints: List<Blueprint>) = measureTimeMillis {
    blueprints.take(3).productOf { evaluate(it, 32) }
        .also(::println)
}

inline fun <T> Iterable<T>.productOf(selector: (T) -> Int): Int {
    var product = 1
    for (element in this) {
        product *= selector(element)
    }
    return product
}

data class CacheEntry(
    val timeLeft: Int,

    val storageOre: Int,
    val storageClay: Int,
    val storageObsidian: Int,
    val storageGeode: Int,

    val robotOre: Int,
    val robotClay: Int,
    val robotObsidian: Int,
    val robotGeode: Int
)

fun evaluate(blueprint: Blueprint, time: Int): Int {

    val resourcesSortedByPriority = Resource.values()
        .sortedByDescending(Resource::priority) + null

    var maxGeodes = 0
    val cache = hashSetOf<CacheEntry>()

    fun loop(
        timeLeft: Int,

        storageOre: Int,
        storageClay: Int,
        storageObsidian: Int,
        storageGeode: Int,

        robotOre: Int,
        robotClay: Int,
        robotObsidian: Int,
        robotGeode: Int
    ) {
        if (timeLeft == 0) {
            maxGeodes = max(maxGeodes, storageGeode)
            return
        }

        val potentialGeodes = storageGeode + timeLeft * ((robotGeode + robotGeode + timeLeft) / 2)
        if (potentialGeodes <= maxGeodes) return

        val cacheEntry = CacheEntry(
            timeLeft,

            storageOre,
            storageClay,
            storageObsidian,
            storageGeode,

            robotOre,
            robotClay,
            robotObsidian,
            robotGeode
        )
        if (cache.contains(cacheEntry)) return
        cache.add(cacheEntry)

        val postProdOre = storageOre + robotOre
        val postProdClay = storageClay + robotClay
        val postProdObsidian = storageObsidian + robotObsidian
        val postProdGeode = storageGeode + robotGeode

        resourcesSortedByPriority.forEach { robotTypeToBuild ->
            if (robotTypeToBuild != null) {
                val cost = blueprint.getCost(robotTypeToBuild)
                val canBuild = storageOre >= cost.getOrDefault(Resource.ORE, 0) &&
                        storageClay >= cost.getOrDefault(Resource.CLAY, 0) &&
                        storageObsidian >= cost.getOrDefault(Resource.OBSIDIAN, 0)

                if (!canBuild) return@forEach

                val newStorageOre = postProdOre - (cost[Resource.ORE] ?: 0)
                val newStorageClay = postProdClay - (cost[Resource.CLAY] ?: 0)
                val newStorageObsidian = postProdObsidian - (cost[Resource.OBSIDIAN] ?: 0)
                val newStorageGeode = postProdGeode - (cost[Resource.GEODE] ?: 0)

                val newRobotOre = if (robotTypeToBuild == Resource.ORE) robotOre + 1 else robotOre
                val newRobotClay = if (robotTypeToBuild == Resource.CLAY) robotClay + 1 else robotClay
                val newRobotObsidian = if (robotTypeToBuild == Resource.OBSIDIAN) robotObsidian + 1 else robotObsidian
                val newRobotGeode = if (robotTypeToBuild == Resource.GEODE) robotGeode + 1 else robotGeode

                loop(
                    timeLeft - 1,

                    newStorageOre,
                    newStorageClay,
                    newStorageObsidian,
                    newStorageGeode,

                    newRobotOre,
                    newRobotClay,
                    newRobotObsidian,
                    newRobotGeode
                )
            } else {
                loop(
                    timeLeft - 1,
                    postProdOre,
                    postProdClay,
                    postProdObsidian,
                    postProdGeode,
                    robotOre,
                    robotClay,
                    robotObsidian,
                    robotGeode
                )
            }
        }
    }

    loop(
        time,

        storageOre = 0,
        storageClay = 0,
        storageObsidian = 0,
        storageGeode = 0,

        robotOre = 1,
        robotClay = 0,
        robotObsidian = 0,
        robotGeode = 0
    )

    return maxGeodes
}
