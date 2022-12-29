package com.anahoret.aoc2022

fun calculateEndIndex(idx: Int, amount: Int, size: Int): Int {
    return calculateEndIndex(idx, amount.toLong(), size)
}

fun calculateEndIndex(idx: Int, amount: Long, size: Int): Int {
    val endIndex = idx + amount
    return when {
        endIndex in 0 until size -> endIndex
        endIndex < 0 -> ((endIndex % size) + size) % size
        else -> endIndex % size
    }.toInt()
}
