package com.anahoret.aoc2022.day07

import java.io.File as JavaIoFile

class FileTree {

    enum class Command {
        CD, LS;

        companion object {
            fun parse(str: String): Command {
                return when {
                    str.startsWith("\$ cd") -> CD
                    str.startsWith("\$ ls") -> LS
                    else -> throw IllegalAccessException("Command does not exist: $str")
                }
            }
        }
    }

    companion object {
        fun buildTree(
            input: List<String>,
            tree: FileTree = FileTree()
        ): FileTree {
            if (input.isEmpty()) return tree

            val line = input.first()
            return when (line.let(Command.Companion::parse)) {
                Command.CD -> handleCd(line, tree, input)
                Command.LS -> handleLs(input, tree)
            }
        }

        private fun handleLs(input: List<String>, tree: FileTree): FileTree {
            val nodes = input.drop(1).takeWhile { !isCommand(it) }
                .map { toNode(it, tree.currentDir) }
            tree.addAll(nodes)
            return buildTree(input.drop(nodes.size + 1), tree)
        }

        private fun handleCd(command: String, tree: FileTree, input: List<String>): FileTree {
            val path = command.split(" ")[2]
            tree.goTo(path)
            return buildTree(input.drop(1), tree)
        }

        private fun toNode(str: String, parent: Directory): FileTreeNode {
            val parts = str.split(" ")
            return if (str.startsWith("dir")) Directory(parts[1], parent)
            else File(parts[1], parts[0].toInt())
        }

        private fun isCommand(strings: String): Boolean {
            return strings.startsWith("$")
        }
    }

    private var root = Directory("/", null)
    var currentDir = root
        private set

    fun goTo(name: String): Directory {
        currentDir = when (name) {
            "/" -> root
            ".." -> currentDir.parent!!
            else -> currentDir.getDir(name) ?: currentDir.add(Directory(name, currentDir))
        }
        return currentDir
    }

    fun addAll(nodes: List<FileTreeNode>) {
        currentDir.addAll(nodes)
    }

    fun findAll(predicate: (FileTreeNode) -> Boolean): List<FileTreeNode> {
        return root.findAll(predicate)
    }

    fun size(): Int {
        return root.size()
    }

}

interface FileTreeNode {
    val name: String
    fun size(): Int
}

open class Directory(override val name: String, val parent: Directory?) : FileTreeNode {

    private val nodes = mutableListOf<FileTreeNode>()

    fun <T : FileTreeNode> add(node: T): T {
        nodes.add(node)
        return node
    }

    override fun size(): Int {
        return nodes.fold(0) { acc, node ->
            acc + node.size()
        }
    }

    fun getDir(name: String): Directory? {
        return nodes.find { it is Directory && it.name == name } as Directory?
    }

    fun addAll(nodes: List<FileTreeNode>) {
        this.nodes.addAll(nodes)
    }

    fun findAll(predicate: (FileTreeNode) -> Boolean): List<FileTreeNode> {
        return nodes.fold(mutableListOf()) { acc, node ->
            if (predicate(node)) acc.add(node)
            if (node is Directory) acc.addAll(node.findAll(predicate))
            acc
        }
    }

}

class File(override val name: String, val size: Int) : FileTreeNode {
    override fun size(): Int {
        return size
    }
}

fun main() {
    val fileTree = JavaIoFile("src/main/kotlin/com/anahoret/aoc2022/day07/input.txt")
        .readText()
        .trim()
        .split("\n")
        .let(FileTree.Companion::buildTree)

    // Part 1
    part1(fileTree)

    // Part 2
    part2(fileTree)
}

private fun part1(fileTree: FileTree) {
    fileTree.findAll {
        it is Directory && it.size() < 100000
    }.sumOf(FileTreeNode::size)
        .let(::println)
}

private fun part2(fileTree: FileTree) {
    val totalSpace = 70000000
    val needForUpdateSpace = 30000000
    val usedSpace = fileTree.size()
    val freeSpace = totalSpace - usedSpace
    val needToFreeSpace = needForUpdateSpace - freeSpace
    fileTree.findAll {
        it is Directory && it.size() >= needToFreeSpace
    }.minBy(FileTreeNode::size)
        .size()
        .let(::println)
}
