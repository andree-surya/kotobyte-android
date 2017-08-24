package com.kotobyte.utils.vector

data class VectorPath(private val commands: List<VectorPathCommand>) {

    val numberOfCommands: Int
        get() = commands.size

    fun getCommand(index: Int) = commands[index]
}
