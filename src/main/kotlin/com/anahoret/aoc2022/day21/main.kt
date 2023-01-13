package com.anahoret.aoc2022.day21

import java.io.File
import kotlin.system.measureTimeMillis

enum class Operation {
    PLUS, MINUS, TIMES, DIVIDE;
}

sealed class Creature(val name: String) {
    abstract fun calculate(): Long
}

sealed class NumberCreature(name: String, val number: Long) : Creature(name) {
    override fun calculate(): Long {
        return number
    }
}

class NumberMonkey(name: String, number: Long) : NumberCreature(name, number)
class Human(name: String, number: Long) : NumberCreature(name, number)

class WaitingMonkey(name: String, val left: Creature, val right: Creature, val operation: Operation) : Creature(name) {
    override fun calculate(): Long {
        return when (operation) {
            Operation.PLUS -> left.calculate() + right.calculate()
            Operation.MINUS -> left.calculate() - right.calculate()
            Operation.TIMES -> left.calculate() * right.calculate()
            Operation.DIVIDE -> left.calculate() / right.calculate()
        }
    }
}

fun parseMonkeys(str: String): WaitingMonkey {
    val operationMap = mapOf(
        "+" to Operation.PLUS,
        "-" to Operation.MINUS,
        "*" to Operation.TIMES,
        "/" to Operation.DIVIDE,
    )

    val rootName = "root"
    val humanName = "humn"

    val creatureCache = mutableMapOf<String, Creature>()
    val nameDataMap = str.split("\n").associate {
        val (name, data) = it.split(": ")
        name to data
    }

    fun parse(name: String, dataLine: String): Creature {
        val cached = creatureCache[name]
        return if (cached != null) {
            cached
        } else {
            val data = dataLine.split(" ")
            when {
                data.size == 1 && name == humanName -> Human(name, data.first().toLong()).also { creatureCache[name] = it }
                data.size == 1 -> NumberMonkey(name, data.first().toLong()).also { creatureCache[name] = it }
                else -> {
                    val (l, o, r) = data
                    val left = parse(l, nameDataMap.getValue(l))
                    val right = parse(r, nameDataMap.getValue(r))
                    WaitingMonkey(name, left, right, operationMap.getValue(o))
                }
            }.also { creatureCache[name] = it }
        }
    }

    return parse(rootName, nameDataMap.getValue(rootName)) as WaitingMonkey
}


fun main() {
    val input = File("src/main/kotlin/com/anahoret/aoc2022/day21/input.txt")
        .readText()
        .trim()

    val rootMonkey = parseMonkeys(input)

    // Part 1
    part1(rootMonkey).also { println("P1: ${it}ms") }

    // Part 2
    part2(rootMonkey).also { println("P2: ${it}ms") }
}

private fun part1(rootMonkey: WaitingMonkey) = measureTimeMillis {
    rootMonkey.calculate().also(::println)
}

private fun part2(rootMonkey: WaitingMonkey) = measureTimeMillis {

    fun hasHuman(creature: Creature): Boolean {
        return when (creature) {
            is Human -> true
            is NumberMonkey -> false
            is WaitingMonkey -> hasHuman(creature.left) || hasHuman(creature.right)
        }
    }

    fun match(goal: Long, creature: Creature): Long {
        return when (creature) {
            is Human -> goal
            is NumberMonkey -> creature.number
            is WaitingMonkey -> {
                if (hasHuman(creature.left)) {
                    val rightValue = creature.right.calculate()
                    val leftGoal = when(creature.operation) {
                        Operation.PLUS -> goal - rightValue
                        Operation.MINUS -> goal + rightValue
                        Operation.TIMES -> goal / rightValue
                        Operation.DIVIDE -> goal * rightValue
                    }
                    match(leftGoal, creature.left)
                } else {
                    val leftValue = creature.left.calculate()
                    val rightGoal = when(creature.operation) {
                        Operation.PLUS -> goal - leftValue
                        Operation.MINUS -> leftValue - goal
                        Operation.TIMES -> goal / leftValue
                        Operation.DIVIDE -> leftValue / goal
                    }
                    match(rightGoal, creature.right)
                }
            }
        }
    }

    if (hasHuman(rootMonkey.left)) {
        val rightValue = rootMonkey.right.calculate()
        match(rightValue, rootMonkey.left)
    } else {
        val leftValue = rootMonkey.left.calculate()
        match(leftValue, rootMonkey.right)
    }.also { println(it) }
}

