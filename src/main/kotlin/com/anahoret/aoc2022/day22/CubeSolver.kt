package com.anahoret.aoc2022.day22

import com.anahoret.aoc2022.Vector3
import com.anahoret.aoc2022.Vector3Int
import com.anahoret.aoc2022.degToRad
import java.lang.RuntimeException
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CubeSolver(private val input: String) {

    fun solve(): List<Int> {
        val (cube, path) = parseInputToCube(input)
        val currentPos = Position3D(0, 0, 0, Vector3Int.RIGTH, CubeSide.BOTTOM)

        path.actions.forEach {
            currentPos.move(cube, it)
        }
        val finishTile3D = cube.tile3DAt(currentPos.x, currentPos.y, currentPos.z, currentPos.cubeSide)
        val unfolded3dFacing = finishTile3D
            .unfoldRotations
            .reversed()
            .fold(currentPos.direction3d) { acc, (axis, rotationDirection) ->
                acc.rotateAround(axis, rotationDirection)
            }
        val facing = when (unfolded3dFacing) {
            Vector3Int.LEFT -> Direction.LEFT
            Vector3Int.RIGTH -> Direction.RIGHT
            Vector3Int.BACKWARD -> Direction.UP
            Vector3Int.FORWARD -> Direction.DOWN
            else -> throw RuntimeException("Unsupported facing $unfolded3dFacing")
        }.facing
        val tile = finishTile3D.tile

        return listOf(tile.row, tile.col, facing)
    }

    private fun parseInputToCube(str: String): Pair<Cube, Path> {
        val (cubeStr, pathStr) = str.split("\n\n")
        val cube = Cube.parseCube(cubeStr)
        return cube to parsePath(pathStr)
    }

}

private enum class Axis { X, Y, Z }

private data class Tile3D(val tile: Tile, var cubeSide: CubeSide, var pos: Vector3, var normal: Vector3) {

    val unfoldRotations = mutableListOf<Pair<Axis, RotationDirection>>()

    fun rotate(axis: Axis, direction: RotationDirection) {
        when (direction) {
            RotationDirection.CW -> rotateCW(axis)
            RotationDirection.CCW -> rotateCCW(axis)
        }
        unfoldRotations.add(axis to direction.opposite())
    }

    private fun updateCubeSide() {
        cubeSide = when (normal.round()) {
            Vector3Int.LEFT -> CubeSide.LEFT
            Vector3Int.RIGTH -> CubeSide.RIGHT
            Vector3Int.UP -> CubeSide.TOP
            Vector3Int.DOWN -> CubeSide.BOTTOM
            Vector3Int.FORWARD -> CubeSide.FRONT
            Vector3Int.BACKWARD -> CubeSide.BACK

            else -> throw RuntimeException("Illegal normal $normal")
        }
    }

    private fun rotateCCW(axis: Axis) {
        pos = pos.rotateCCW(axis)
        normal = normal.rotateCCW(axis)
        updateCubeSide()
    }

    private fun rotateCW(axis: Axis) {
        pos = pos.rotateCW(axis)
        normal = normal.rotateCW(axis)
        updateCubeSide()
    }

    fun move(dv: Vector3) {
        pos += dv
    }

    fun move(dv: Vector3Int) {
        pos += dv
    }

}

private enum class CubeSide {
    BOTTOM, TOP, LEFT, RIGHT, FRONT, BACK
}

private data class Position3D(
    var x: Int,
    var y: Int,
    var z: Int,
    var direction3d: Vector3Int,
    var cubeSide: CubeSide
)

private class Cube(size: Int, tiles: List<Tile3D>) {

    companion object {
        fun parseCube(cubeStr: String): Cube {
            val tiles = parseTiles(cubeStr)
            val tilesList = tiles.flatten()
            val startTile = tiles[0][0]

            val cubeSize = sqrt(tiles.sumOf { it.size } / 6.0).toInt()
            val cubeRange = 0 until cubeSize

            val tileTo3d = mutableMapOf<Tile, Tile3D>()

            fun outsideOfCube(tile3D: Tile3D): Boolean {
                return tile3D.pos.x.roundToInt() !in cubeRange ||
                        tile3D.pos.y.roundToInt() !in cubeRange ||
                        tile3D.pos.z.roundToInt() !in cubeRange
            }


            for (tile in tilesList) {
                tileTo3d[tile] =
                    Tile3D(tile, CubeSide.BOTTOM, Vector3(tile.x - startTile.x, tile.y - startTile.y, 0), Vector3.DOWN)
            }

            fun foldCube(
                outFilter: (Vector3) -> Boolean,
                oppositeVector: Vector3,
                rotationMap: Map<Vector3Int, Pair<Axis, RotationDirection>>
            ) {
                val out = tileTo3d.values.filter { outFilter(it.pos) }.takeIf { it.isNotEmpty() } ?: return
                val closestToStart = out.minBy { it.tile.manhattanDistance(startTile) }
                val outNormal = closestToStart.normal.round()
                val outOriginVector = closestToStart.pos.round()
                out.forEach { tile3D ->
                    tile3D.move(-outOriginVector)
                    val (axis, direction) = rotationMap.getValue(outNormal)
                    tile3D.rotate(axis, direction)
                    tile3D.move(outOriginVector)
                    tile3D.move(oppositeVector)
                }
            }

            while (tileTo3d.values.any(::outsideOfCube)) {
                foldCube(
                    { it.x.roundToInt() < 0 }, Vector3.RIGHT, mapOf(
                        Vector3Int.FORWARD to RotationAxis.ZCW,
                        Vector3Int.BACKWARD to RotationAxis.ZCCW,
                        Vector3Int.UP to RotationAxis.YCCW,
                        Vector3Int.DOWN to RotationAxis.YCW
                    )
                )
                foldCube(
                    { it.x.roundToInt() > cubeRange.last }, Vector3.LEFT, mapOf(
                        Vector3Int.FORWARD to RotationAxis.ZCCW,
                        Vector3Int.BACKWARD to RotationAxis.ZCW,
                        Vector3Int.UP to RotationAxis.YCW,
                        Vector3Int.DOWN to RotationAxis.YCCW
                    )
                )
                foldCube(
                    { it.y.roundToInt() < 0 }, Vector3.FORWARD, mapOf(
                        Vector3Int.LEFT to RotationAxis.ZCW,
                        Vector3Int.RIGTH to RotationAxis.ZCCW,
                        Vector3Int.UP to RotationAxis.XCW,
                        Vector3Int.DOWN to RotationAxis.XCCW
                    )
                )
                foldCube(
                    { it.y.roundToInt() > cubeRange.last }, Vector3.BACKWARD, mapOf(
                        Vector3Int.LEFT to RotationAxis.ZCCW,
                        Vector3Int.RIGTH to RotationAxis.ZCW,
                        Vector3Int.UP to RotationAxis.XCCW,
                        Vector3Int.DOWN to RotationAxis.XCW
                    )
                )
                foldCube(
                    { it.z.roundToInt() < 0 }, Vector3.UP, mapOf(
                        Vector3Int.LEFT to RotationAxis.YCCW,
                        Vector3Int.RIGTH to RotationAxis.YCW,
                        Vector3Int.BACKWARD to RotationAxis.XCW,
                        Vector3Int.FORWARD to RotationAxis.XCCW
                    )
                )
                foldCube(
                    { it.z.roundToInt() > cubeRange.last }, Vector3.DOWN, mapOf(
                        Vector3Int.LEFT to RotationAxis.YCW,
                        Vector3Int.RIGTH to RotationAxis.YCCW,
                        Vector3Int.BACKWARD to RotationAxis.XCCW,
                        Vector3Int.FORWARD to RotationAxis.XCW
                    )
                )
            }

            return Cube(cubeSize, tileTo3d.values.toList())
        }
    }

    val maxIndex = size - 1

    private val tileLookup = tiles
        .groupBy { it.cubeSide }
        .mapValues { (_, tiles) -> tiles.associateBy { it.pos.round() } }

    fun tileAt(x: Int, y: Int, z: Int, cubeSide: CubeSide): Tile {
        return tile3DAt(x, y, z, cubeSide).tile
    }

    fun tile3DAt(x: Int, y: Int, z: Int, cubeSide: CubeSide): Tile3D {
        return tileLookup.getValue(cubeSide).getValue(Vector3Int(x, y, z))
    }

}

private fun Vector3.rotateCCW(axis: Axis): Vector3 {
    val rotVec = when (axis) {
        Axis.X -> Vector3.LEFT
        Axis.Y -> Vector3.BACKWARD
        Axis.Z -> Vector3.DOWN
    }
    return this.rotateAround(rotVec, 90.degToRad())
}

private fun Vector3.rotateCW(axis: Axis): Vector3 {
    val rotVec = when (axis) {
        Axis.X -> Vector3.RIGHT
        Axis.Y -> Vector3.FORWARD
        Axis.Z -> Vector3.UP
    }
    return this.rotateAround(rotVec, 90.degToRad())
}

private fun Vector3Int.rotateCCW(axis: Axis): Vector3Int {
    return Vector3(x, y, z).rotateCCW(axis).round()
}

private fun Vector3Int.rotateCW(axis: Axis): Vector3Int {
    return Vector3(x, y, z).rotateCW(axis).round()
}

private object RotationAxis {
    val XCW = Axis.X to RotationDirection.CW
    val XCCW = Axis.X to RotationDirection.CCW

    val YCW = Axis.Y to RotationDirection.CW
    val YCCW = Axis.Y to RotationDirection.CCW

    val ZCW = Axis.Z to RotationDirection.CW
    val ZCCW = Axis.Z to RotationDirection.CCW
}

private fun Position3D.move(cube: Cube, action: Action) {

    fun moveRight() {
        when {
            x < cube.maxIndex && cube.tileAt(x + 1, y, z, cubeSide) is OpenTile -> x += 1
            x == cube.maxIndex && cube.tileAt(x, y, z, CubeSide.RIGHT) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.BOTTOM -> Vector3Int.UP
                    CubeSide.TOP -> Vector3Int.DOWN
                    CubeSide.FRONT -> Vector3Int.BACKWARD
                    CubeSide.BACK -> Vector3Int.FORWARD
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.RIGHT}")
                }
                cubeSide = CubeSide.RIGHT
            }
        }
    }

    fun moveLeft() {
        when {
            x > 0 && cube.tileAt(x - 1, y, z, cubeSide) is OpenTile -> x -= 1
            x == 0 && cube.tileAt(x, y, z, CubeSide.LEFT) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.BOTTOM -> Vector3Int.UP
                    CubeSide.TOP -> Vector3Int.DOWN
                    CubeSide.FRONT -> Vector3Int.BACKWARD
                    CubeSide.BACK -> Vector3Int.FORWARD
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.LEFT}")
                }
                cubeSide = CubeSide.LEFT
            }
        }
    }

    fun moveForward() {
        when {
            y < cube.maxIndex && cube.tileAt(x, y + 1, z, cubeSide) is OpenTile -> y += 1
            y == cube.maxIndex && cube.tileAt(x, y, z, CubeSide.FRONT) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.BOTTOM -> Vector3Int.UP
                    CubeSide.TOP -> Vector3Int.DOWN
                    CubeSide.LEFT -> Vector3Int.RIGTH
                    CubeSide.RIGHT -> Vector3Int.LEFT
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.FRONT}")
                }
                cubeSide = CubeSide.FRONT
            }
        }
    }

    fun moveBackward() {
        when {
            y > 0 && cube.tileAt(x, y - 1, z, cubeSide) is OpenTile -> y -= 1
            y == 0 && cube.tileAt(x, y, z, CubeSide.BACK) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.BOTTOM -> Vector3Int.UP
                    CubeSide.TOP -> Vector3Int.DOWN
                    CubeSide.LEFT -> Vector3Int.RIGTH
                    CubeSide.RIGHT -> Vector3Int.LEFT
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.BACK}")
                }
                cubeSide = CubeSide.BACK
            }
        }
    }

    fun moveUp() {
        when {
            z < cube.maxIndex && cube.tileAt(x, y, z + 1, cubeSide) is OpenTile -> z += 1
            z == cube.maxIndex && cube.tileAt(x, y, z, CubeSide.TOP) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.LEFT -> Vector3Int.RIGTH
                    CubeSide.RIGHT -> Vector3Int.LEFT
                    CubeSide.FRONT -> Vector3Int.BACKWARD
                    CubeSide.BACK -> Vector3Int.FORWARD
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.TOP}")
                }
                cubeSide = CubeSide.TOP
            }
        }
    }

    fun moveDown() {
        when {
            z > 0 && cube.tileAt(x, y, z - 1, cubeSide) is OpenTile -> z -= 1
            z == 0 && cube.tileAt(x, y, z, CubeSide.BOTTOM) is OpenTile -> {
                direction3d = when (cubeSide) {
                    CubeSide.LEFT -> Vector3Int.RIGTH
                    CubeSide.RIGHT -> Vector3Int.LEFT
                    CubeSide.FRONT -> Vector3Int.BACKWARD
                    CubeSide.BACK -> Vector3Int.FORWARD
                    else -> throw RuntimeException("Unsupported transfer: $cubeSide ${CubeSide.BOTTOM}")
                }
                cubeSide = CubeSide.BOTTOM
            }
        }
    }

    fun move(steps: Int) {
        repeat(steps) {
            when (direction3d) {
                Vector3Int.LEFT -> moveLeft()
                Vector3Int.RIGTH -> moveRight()
                Vector3Int.UP -> moveUp()
                Vector3Int.DOWN -> moveDown()
                Vector3Int.FORWARD -> moveForward()
                Vector3Int.BACKWARD -> moveBackward()
            }
        }
    }

    when (action) {
        is Movement -> move(action.steps)
        is Rotation -> direction3d = rotation3DMap.getValue(cubeSide to action.direction)(direction3d)
    }
}

private fun Vector3Int.rotateAround(axis: Axis, rotationDirection: RotationDirection): Vector3Int {
    val vector = when (axis) {
        Axis.X -> Vector3.RIGHT
        Axis.Y -> Vector3.FORWARD
        Axis.Z -> Vector3.UP
    }
    val theta = when (rotationDirection) {
        RotationDirection.CW -> 90.0
        RotationDirection.CCW -> -90.0
    }
    return rotateAround(vector, theta)
}

private val rotation3DMap = mapOf<Pair<CubeSide, Direction>, Vector3Int.() -> Vector3Int>(
    (CubeSide.BOTTOM to Direction.RIGHT) to { rotateCW(Axis.Z) },
    (CubeSide.BOTTOM to Direction.LEFT) to { rotateCCW(Axis.Z) },
    (CubeSide.TOP to Direction.RIGHT) to { rotateCCW(Axis.Z) },
    (CubeSide.TOP to Direction.LEFT) to { rotateCW(Axis.Z) },
    (CubeSide.LEFT to Direction.RIGHT) to { rotateCW(Axis.X) },
    (CubeSide.LEFT to Direction.LEFT) to { rotateCCW(Axis.X) },
    (CubeSide.RIGHT to Direction.RIGHT) to { rotateCCW(Axis.X) },
    (CubeSide.RIGHT to Direction.LEFT) to { rotateCW(Axis.X) },
    (CubeSide.FRONT to Direction.RIGHT) to { rotateCCW(Axis.Y) },
    (CubeSide.FRONT to Direction.LEFT) to { rotateCW(Axis.Y) },
    (CubeSide.BACK to Direction.RIGHT) to { rotateCW(Axis.Y) },
    (CubeSide.BACK to Direction.LEFT) to { rotateCCW(Axis.Y) },
)

private enum class RotationDirection {
    CW, CCW;

    fun opposite(): RotationDirection {
        return when (this) {
            CW -> CCW
            CCW -> CW
        }
    }
}
