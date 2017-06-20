package com.kotobyte.utils.vector;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VectorPathParserTest {
    private static final float EPSILON = 0.0001f;

    private VectorPathParser mParser;

    @Before
    public void prepareParser() {
        mParser = new VectorPathParser();
    }

    @Test
    public void parse_shouldParseMoveCommand() throws Exception {

        VectorPath path = mParser.parse("M12.7532,65.67m-67.1,82");
        assertEquals(2, path.getNumberOfCommands());

        VectorPathCommand.Move command0 = (VectorPathCommand.Move) path.getCommand(0);
        VectorPathCommand.Move command1 = (VectorPathCommand.Move) path.getCommand(1);

        assertFalse(command0.isRelative());
        assertEquals(12.7532, command0.getX(), EPSILON);
        assertEquals(65.67, command0.getY(), EPSILON);

        assertTrue(command1.isRelative());
        assertEquals(-67.1, command1.getX(), EPSILON);
        assertEquals(82, command1.getY(), EPSILON);
    }

    @Test
    public void parse_shouldParseCubicCommand() throws Exception {

        VectorPath path = mParser.parse("C11.7,22.3,23.4,-24.6,30.1,-34.5s40.1,42.2,45.5,51.0S10,-20,-30,4.5");
        assertEquals(3, path.getNumberOfCommands());

        VectorPathCommand.Cubic command0 = (VectorPathCommand.Cubic) path.getCommand(0);
        VectorPathCommand.Cubic command1 = (VectorPathCommand.Cubic) path.getCommand(1);
        VectorPathCommand.Cubic command2 = (VectorPathCommand.Cubic) path.getCommand(2);

        assertFalse(command0.isRelative());
        assertEquals(11.7, command0.getX1(), EPSILON);
        assertEquals(22.3, command0.getY1(), EPSILON);
        assertEquals(23.4, command0.getX2(), EPSILON);
        assertEquals(-24.6, command0.getY2(), EPSILON);
        assertEquals(30.1, command0.getX3(), EPSILON);
        assertEquals(-34.5, command0.getY3(), EPSILON);

        assertTrue(command1.isRelative());
        assertEquals(30.1 - 23.4, command1.getX1(), EPSILON);
        assertEquals(-34.5 - -24.6, command1.getY1(), EPSILON);
        assertEquals(40.1, command1.getX2(), EPSILON);
        assertEquals(42.2, command1.getY2(), EPSILON);
        assertEquals(45.5, command1.getX3(), EPSILON);
        assertEquals(51.0, command1.getY3(), EPSILON);

        assertFalse(command2.isRelative());
        assertEquals(45.5 - 40.1, command2.getX1(), EPSILON);
        assertEquals(51 - 42.2, command2.getY1(), EPSILON);
        assertEquals(10, command2.getX2(), EPSILON);
        assertEquals(-20, command2.getY2(), EPSILON);
        assertEquals(-30, command2.getX3(), EPSILON);
        assertEquals(4.5, command2.getY3(), EPSILON);
    }

    @Test
    public void parse_shouldHandleWhitespacesAndCommas() throws Exception {

        VectorPath path = mParser.parse("M  32.64   82.12,c  12.3,14.5 67.1,-82.1 23.20  10");
        assertEquals(2, path.getNumberOfCommands());

        VectorPathCommand.Move command0 = (VectorPathCommand.Move) path.getCommand(0);
        VectorPathCommand.Cubic command1 = (VectorPathCommand.Cubic) path.getCommand(1);

        assertFalse(command0.isRelative());
        assertEquals(32.64, command0.getX(), EPSILON);
        assertEquals(82.12, command0.getY(), EPSILON);

        assertTrue(command1.isRelative());
        assertEquals(12.3, command1.getX1(), EPSILON);
        assertEquals(14.5, command1.getY1(), EPSILON);
        assertEquals(67.1, command1.getX2(), EPSILON);
        assertEquals(-82.1, command1.getY2(), EPSILON);
        assertEquals(23.20, command1.getX3(), EPSILON);
        assertEquals(10, command1.getY3(), EPSILON);
    }
}