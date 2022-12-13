package com.anahoret.aoc2022.day13

import java.io.File
import java.lang.StringBuilder

private fun String.isListLiteral(): Boolean {
    return startsWith("[") && endsWith("]")
}

private fun String.unwrapListLiteral(): String {
    return drop(1).dropLast(1)
}

sealed class IntOrList : Comparable<IntOrList> {

    companion object {
        fun parse(str: String): IntOrList {
            val parser = if (str.isListLiteral()) ListWrapper::parse else IntWrapper::parse
            return parser(str)
        }
    }

}

class IntWrapper(private val value: Int) : IntOrList() {

    companion object {
        fun parse(str: String): IntOrList {
            return IntWrapper(str.toInt())
        }
    }

    override fun compareTo(other: IntOrList): Int {
        return when (other) {
            is IntWrapper -> value.compareTo(other.value)
            is ListWrapper -> toList().compareTo(other)
        }
    }

    fun toList(): ListWrapper {
        return ListWrapper(listOf(this))
    }

    override fun toString(): String {
        return value.toString()
    }
}

class ListWrapper(private val value: List<IntOrList>) : IntOrList() {

    companion object {
        fun parse(str: String): IntOrList {
            val unwrapped = str.unwrapListLiteral()
            if (unwrapped.isEmpty()) return ListWrapper(emptyList())
            val elements = unwrapped.fold(0 to listOf(StringBuilder())) { (nestLevel, acc), c ->
                handleChar(c, nestLevel, acc)
            }.second
                .map(StringBuilder::toString)
                .map(IntOrList::parse)
            return ListWrapper(elements)
        }

        private fun handleChar(c: Char, nestLevel: Int, acc: List<StringBuilder>): Pair<Int, List<StringBuilder>> {
            return when (c) {
                ',' -> {
                    when (nestLevel) {
                        0 -> 0 to acc + StringBuilder()
                        else -> nestLevel to acc.appendToLast(c)
                    }
                }

                '[' -> nestLevel + 1 to acc.appendToLast(c)
                ']' -> nestLevel - 1 to acc.appendToLast(c)
                else -> nestLevel to acc.appendToLast(c)
            }
        }
    }

    override fun compareTo(other: IntOrList): Int {
        return when (other) {
            is IntWrapper -> compareTo(other.toList())
            is ListWrapper -> doCompare(other)
        }
    }

    private fun doCompare(other: ListWrapper): Int {
        for (idx in value.indices) {
            if (idx !in other.value.indices) {
                return 1
            } else {
                val cmpRes = value[idx].compareTo(other.value[idx])
                if (cmpRes != 0) return cmpRes
            }
        }
        return if (value.size == other.value.size) 0 else -1
    }


    override fun toString(): String {
        return value.toString()
    }
}

fun List<StringBuilder>.appendToLast(char: Char): List<StringBuilder> {
    last().append(char)
    return this
}

fun parseInput(str: String): List<Pair<IntOrList, IntOrList>> {
    return str.split("\n\n")
        .map {
            val (l, r) = it.split("\n")
            IntOrList.parse(l) to IntOrList.parse(r)
        }
}

fun main() {
    val lists = File("src/main/kotlin/com/anahoret/aoc2022/day13/input.txt")
        .readText()
        .trim()
        .let(::parseInput)

    // Part 1
    part1(lists)

    // Part 2
    part2(lists)
}

private fun part1(input: List<Pair<IntOrList, IntOrList>>) {
    input.withIndex()
        .filter { (_, lr) -> lr.first < lr.second }
        .sumOf { it.index.inc() }
        .let(::println)
}

private fun part2(input: List<Pair<IntOrList, IntOrList>>) {
    val dividerPacket1 = IntWrapper(2).toList()
    val dividerPacket2 = IntWrapper(6).toList()
    input.flatMap { listOf(it.first, it.second) }
        .let { it + dividerPacket1 + dividerPacket2 }
        .sorted()
        .let { it.indexOf(dividerPacket1).inc() * it.indexOf(dividerPacket2).inc() }
        .let(::println)
}



