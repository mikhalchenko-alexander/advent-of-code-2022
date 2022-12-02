package com.anahoret.aoc2022.day02

import java.io.File

enum class Choice(val points: Int) {
    ROCK(1),
    PAPER(2),
    SCISSORS(3)
}

enum class Outcome(val points: Int) {
    LOOSE(0),
    DRAW(3),
    WIN(6)
}

val opponentChoice = mapOf(
    "A" to Choice.ROCK,
    "B" to Choice.PAPER,
    "C" to Choice.SCISSORS,
)

val looseMap = mapOf(
    Choice.ROCK to Choice.SCISSORS,
    Choice.PAPER to Choice.ROCK,
    Choice.SCISSORS to Choice.PAPER,
)

class Round(opponentChoice: Choice, ownChoice: Choice) {

    companion object {
        private val ownChoice = mapOf(
            "X" to Choice.ROCK,
            "Y" to Choice.PAPER,
            "Z" to Choice.SCISSORS,
        )

        fun parse(str: String): Round {
            val split = str.split(" ")
            return Round(
                opponentChoice.getValue(split[0]),
                ownChoice.getValue(split[1])
            )
        }
    }

    private val outcomePoints: Int = (
            if (opponentChoice == ownChoice) Outcome.DRAW
            else if (looseMap.getValue(opponentChoice) == ownChoice) Outcome.LOOSE
            else Outcome.WIN
            ).points

    val points = ownChoice.points + outcomePoints
}

class NeededOutcome(opponentChoice: Choice, neededOutcome: Outcome) {

    companion object {
        private val neededOutcome = mapOf(
            "X" to Outcome.LOOSE,
            "Y" to Outcome.DRAW,
            "Z" to Outcome.WIN,
        )

        private val winMap = mapOf(
            Choice.ROCK to Choice.PAPER,
            Choice.PAPER to Choice.SCISSORS,
            Choice.SCISSORS to Choice.ROCK,
        )

        fun parse(str: String): NeededOutcome {
            val split = str.split(" ")
            return NeededOutcome(opponentChoice.getValue(split[0]), neededOutcome.getValue(split[1]))
        }
    }

    private val ownChoice = when (neededOutcome) {
        Outcome.LOOSE -> looseMap.getValue(opponentChoice)
        Outcome.DRAW -> opponentChoice
        Outcome.WIN -> winMap.getValue(opponentChoice)
    }
    val round = Round(opponentChoice, ownChoice)
}

// https://adventofcode.com/2022/day/2
fun main() {
    val roundsData = File("src/main/kotlin/com/anahoret/aoc2022/day02/input.txt")
        .readText()
        .split("\n")
        .filter(String::isNotEmpty)

    // Part 1
    roundsData
        .map(Round::parse)
        .sumOf(Round::points)
        .let(::println)

    // Part 2
    roundsData
        .map(NeededOutcome::parse)
        .map(NeededOutcome::round)
        .sumOf(Round::points)
        .let(::println)
}
