package com.kotobyte.utils.vector

import java.util.ArrayList

class VectorPathParser {

    private var currentString: String = ""
    private var currentOffset: Int = 0
    private var currentCommandCode: Char = ' '

    fun parse(strings: List<String>): List<VectorPath> = strings.map { parse(it) }

    fun parse(string: String): VectorPath {

        val commands = ArrayList<VectorPathCommand>()

        currentString = string
        currentOffset = 0
        currentCommandCode = ' '

        // Helper variables for parsing SVG cubic bezier directives.
        var lastX2 = 0f
        var lastY2 = 0f
        var lastX3 = 0f
        var lastY3 = 0f

        while (moveToNextCommand()) {

            val c = currentCommandCode
            val isRelative = Character.isLowerCase(c)

            // SVG move directives.
            if (c == 'M' || c == 'm') {
                val x = nextNumber()
                val y = nextNumber()

                commands.add(VectorPathCommand.Move(x, y, isRelative))
            }

            // SVG cubic bezier directives
            if (c == 'C' || c == 'c' || c == 'S' || c == 's') {

                var x1 = lastX3 - lastX2
                var y1 = lastY3 - lastY2

                if (c == 'C' || c == 'c') {
                    x1 = nextNumber()
                    y1 = nextNumber()
                }

                val x2 = nextNumber()
                val y2 = nextNumber()
                val x3 = nextNumber()
                val y3 = nextNumber()

                lastX2 = x2
                lastY2 = y2
                lastX3 = x3
                lastY3 = y3

                commands.add(VectorPathCommand.Cubic(x1, y1, x2, y2, x3, y3, isRelative))
            }
        }

        return VectorPath(commands)
    }

    private fun moveToNextCommand(): Boolean {

        while (currentOffset < currentString.length) {
            val c = currentString[currentOffset]

            if (Character.isLetter(c)) {
                // A command directive encountered. Update offset and report command.

                currentOffset += 1
                currentCommandCode = c

                return true
            }

            if ((Character.isDigit(c) || c == '-') && currentCommandCode.toInt() > 0) {
                // Number character encountered. Proceed with most recent command.

                return true
            }

            currentOffset++
        }

        return false
    }

    private fun nextNumber(): Float {

        var numberBeginIndex = -1
        var numberEndIndex = -1

        while (currentOffset < currentString.length && numberEndIndex < 0) {
            val c = currentString[currentOffset]

            if (numberBeginIndex < 0 && c == '-' || Character.isDigit(c) || c == '.') {
                // A valid decimal number encountered. Record start index if needed and go on.

                if (numberBeginIndex < 0) {
                    numberBeginIndex = currentOffset
                }

                currentOffset++

            } else if (numberBeginIndex < 0) {
                // We haven't met any number. Go on searching.
                currentOffset++

            } else {
                // We just completed a number. Stop searching.
                numberEndIndex = currentOffset
            }
        }

        if (numberBeginIndex < 0) {
            // Expecting a number but we didn't find any.
            throwStringFormatException()
        }

        if (numberEndIndex < 0) {
            // Handle number at the end of currentString.
            numberEndIndex = currentString.length
        }

        return java.lang.Float.parseFloat(currentString.substring(numberBeginIndex, numberEndIndex))
    }

    private fun throwStringFormatException(): Nothing =
            throw IllegalArgumentException("Unsupported vector path format: " + currentString)
}
