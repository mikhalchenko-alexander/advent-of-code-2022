package com.anahoret.aoc2022

import kotlin.math.*

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

interface ManhattanDistanceAware {
    val x: Int
    val y: Int

    fun xDistance(other: ManhattanDistanceAware): Int {
        return abs(x - other.x)
    }

    fun yDistance(other: ManhattanDistanceAware): Int {
        return abs(y - other.y)
    }

    fun manhattanDistance(other: ManhattanDistanceAware): Int {
        return xDistance(other) + yDistance(other)
    }
}

data class Vector3Int(val x: Int, val y: Int, val z: Int) {
    companion object {
        val RIGTH = Vector3Int(1, 0, 0)
        val FORWARD = Vector3Int(0, 1, 0)
        val UP = Vector3Int(0, 0, 1)
        val LEFT = Vector3Int(-1, 0, 0)
        val BACKWARD = Vector3Int(0, -1, 0)
        val DOWN = Vector3Int(0, 0, -1)
    }

    operator fun unaryMinus(): Vector3Int {
        return Vector3Int(-x, -y, -z)
    }

    fun rotateAround(axis: Vector3, theta: Double): Vector3Int {
        return Vector3(x, y, z).rotateAround(axis, theta).round()
    }
}

data class Vector3(val x: Double, val y: Double, val z: Double) {

    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())

    companion object {
        val RIGHT = Vector3(1, 0, 0)
        val FORWARD = Vector3(0, 1, 0)
        val UP = Vector3(0, 0, 1)
        val LEFT = Vector3(-1, 0, 0)
        val BACKWARD = Vector3(0, -1, 0)
        val DOWN = Vector3(0, 0, -1)
    }

    infix fun dot(other: Vector3): Double {
        return x * other.x + y * other.y + z * other.z
    }

    infix fun cross(other: Vector3): Vector3 {
        val cpx = this.y * other.z - this.z * other.y
        val cpy = this.z * other.x - this.x * other.z
        val cpz = this.x * other.y - this.y * other.x
        return Vector3(cpx, cpy, cpz)
    }

    operator fun unaryMinus(): Vector3 {
        return Vector3(-x, -y, -z)
    }

    operator fun times(n: Double): Vector3 {
        return Vector3(x * n, y * n, z * n)
    }

    operator fun times(n: Int): Vector3 {
        return Vector3(x * n, y * n, z * n)
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    operator fun plus(other: Vector3Int): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    fun rotateAround(axis: Vector3, theta: Double): Vector3 {
        val cosTheta = cos(theta)
        val sinTheta = sin(theta)
        return this * cosTheta + (axis cross this) * sinTheta + axis * (axis dot this) * (1 - cosTheta)
    }

    fun round(): Vector3Int {
        return Vector3Int(x.roundToInt(), y.roundToInt(), z.roundToInt())
    }

    override fun toString(): String {
        return "[$x, $y, $z]"
    }

}

operator fun Double.times(v: Vector3): Vector3 {
    return Vector3(v.x * this, v.y * this, v.z * this)
}


fun Int.degToRad() = degToRad(this.toDouble())
fun degToRad(deg: Double) = Math.toRadians(deg)

//fun main() {
//    val v = Vector3(1, 0, 0)
//    val ap = Vector3(0, 0, 1)
//    val an = Vector3(0, 0, -1)
//    val vrp = v.rotateAround(ap, 90.degToRad()).round()
//    val vrn = v.rotateAround(an, 90.degToRad()).round()
//    println(vrp)
//    println(vrn)
//}
