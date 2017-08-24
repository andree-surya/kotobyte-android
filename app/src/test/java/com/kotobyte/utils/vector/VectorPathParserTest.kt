package com.kotobyte.utils.vector

import org.junit.Assert.*
import org.junit.Test


class VectorPathParserTest {

    @Test
    fun parse_shouldParseMoveCommand() {

        val path = VectorPathParser().parse("M12.7532,65.67m-67.1,82")
        assertEquals(2, path.numberOfCommands.toLong())

        val (x0, y0, isRelative0) = path.getCommand(0) as VectorPathCommand.Move
        val (x1, y1, isRelative1) = path.getCommand(1) as VectorPathCommand.Move

        assertFalse(isRelative0)
        assertEquals(12.7532f, x0, EPSILON)
        assertEquals(65.67f, y0, EPSILON)

        assertTrue(isRelative1)
        assertEquals(-67.1f, x1, EPSILON)
        assertEquals(82f, y1, EPSILON)
    }

    @Test
    fun parse_shouldParseCubicCommand() {

        val path = VectorPathParser().parse("C11.7,22.3,23.4,-24.6,30.1,-34.5s40.1,42.2,45.5,51.0S10,-20,-30,4.5")
        assertEquals(3, path.numberOfCommands.toLong())

        val (x10, y10, x20, y20, x30, y30, isRelative0) = path.getCommand(0) as VectorPathCommand.Cubic
        val (x11, y11, x21, y21, x31, y31, isRelative1) = path.getCommand(1) as VectorPathCommand.Cubic
        val (x12, y12, x22, y22, x32, y32, isRelative2) = path.getCommand(2) as VectorPathCommand.Cubic

        assertFalse(isRelative0)
        assertEquals(11.7f, x10, EPSILON)
        assertEquals(22.3f, y10, EPSILON)
        assertEquals(23.4f, x20, EPSILON)
        assertEquals(-24.6f, y20, EPSILON)
        assertEquals(30.1f, x30, EPSILON)
        assertEquals(-34.5f, y30, EPSILON)

        assertTrue(isRelative1)
        assertEquals(30.1f - 23.4f, x11, EPSILON)
        assertEquals(-34.5f - -24.6f, y11, EPSILON)
        assertEquals(40.1f, x21, EPSILON)
        assertEquals(42.2f, y21, EPSILON)
        assertEquals(45.5f, x31, EPSILON)
        assertEquals(51.0f, y31, EPSILON)

        assertFalse(isRelative2)
        assertEquals(45.5f - 40.1f, x12, EPSILON)
        assertEquals(51f - 42.2f, y12, EPSILON)
        assertEquals(10f, x22, EPSILON)
        assertEquals(-20f, y22, EPSILON)
        assertEquals(-30f, x32, EPSILON)
        assertEquals(4.5f, y32, EPSILON)
    }

    @Test
    fun parse_shouldHandleWhitespacesAndCommas() {

        val path = VectorPathParser().parse("M  32.64   82.12,c  12.3,14.5 67.1,-82.1 23.20  10")
        assertEquals(2, path.numberOfCommands.toLong())

        val (x0, y0, isRelative0) = path.getCommand(0) as VectorPathCommand.Move
        val (x1, y1, x2, y2, x3, y3, isRelative1) = path.getCommand(1) as VectorPathCommand.Cubic

        assertFalse(isRelative0)
        assertEquals(32.64f, x0, EPSILON)
        assertEquals(82.12f, y0, EPSILON)

        assertTrue(isRelative1)
        assertEquals(12.3f, x1, EPSILON)
        assertEquals(14.5f, y1, EPSILON)
        assertEquals(67.1f, x2, EPSILON)
        assertEquals(-82.1f, y2, EPSILON)
        assertEquals(23.20f, x3, EPSILON)
        assertEquals(10f, y3, EPSILON)
    }

    companion object {
        private val EPSILON = 0.0001f
    }
}