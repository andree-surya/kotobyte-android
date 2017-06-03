package com.kotobyte.utils.vector;

import java.util.ArrayList;
import java.util.List;

public class VectorPathParser {

    private String mString;
    private int mCurrentOffset;
    private char mCurrentCommandCode;

    public static List<VectorPath> parse(List<String> strings) {

        VectorPathParser parser = new VectorPathParser();
        List<VectorPath> vectorPaths = new ArrayList<>(strings.size());

        for (String string : strings) {
            vectorPaths.add(parser.parse(string));
        }

        return vectorPaths;
    }

    public VectorPath parse(String string) {

        List<VectorPathCommand> commands = new ArrayList<>();

        mString = string;
        mCurrentOffset = 0;
        mCurrentCommandCode = 0;

        // Helper variables for parsing SVG cubic bezier directives.
        float lastX2 = 0;
        float lastY2 = 0;
        float lastX3 = 0;
        float lastY3 = 0;

        while (moveToNextCommand()) {
            char c = mCurrentCommandCode;
            boolean isRelative = Character.isLowerCase(c);

            // SVG move directives.
            if (c == 'M' || c == 'm') {
                float x = nextNumber();
                float y = nextNumber();

                commands.add(new VectorPathCommand.Move(x, y, isRelative));
            }

            // SVG cubic bezier directives
            if (c == 'C' || c == 'c' || c == 'S' || c == 's') {

                float x1 = lastX3 - lastX2;
                float y1 = lastY3 - lastY2;

                if (c == 'C' || c == 'c') {
                    x1 = nextNumber();
                    y1 = nextNumber();
                }

                float x2 = lastX2 = nextNumber();
                float y2 = lastY2 = nextNumber();
                float x3 = lastX3 = nextNumber();
                float y3 = lastY3 = nextNumber();

                commands.add(new VectorPathCommand.Cubic(x1, y1, x2, y2, x3, y3, isRelative));
            }
        }

        return new VectorPath(commands);
    }

    private boolean moveToNextCommand() {

        while (mCurrentOffset < mString.length()) {
            char c = mString.charAt(mCurrentOffset);

            if (Character.isLetter(c)) {
                // A command directive encountered. Update offset and report command.

                mCurrentOffset += 1;
                mCurrentCommandCode = c;

                return true;
            }

            if ((Character.isDigit(c) || c == '-') && mCurrentCommandCode > 0) {
                // Number character encountered. Proceed with most recent command.

                return true;
            }

            mCurrentOffset++;
        }

        return false;
    }

    private float nextNumber() {

        int numberBeginIndex = -1;
        int numberEndIndex = -1;

        while (mCurrentOffset < mString.length() && numberEndIndex < 0) {
            char c = mString.charAt(mCurrentOffset);

            if ((numberBeginIndex < 0 && c == '-') || Character.isDigit(c) || c == '.') {
                // A valid decimal number encountered. Record start index if needed and go on.

                if (numberBeginIndex < 0) {
                    numberBeginIndex = mCurrentOffset;
                }

                mCurrentOffset++;

            } else if (numberBeginIndex < 0) {
                // We haven't meet any number. Go on searching.
                mCurrentOffset++;

            } else {
                // We just completed a number. Stop searching.
                numberEndIndex = mCurrentOffset;
            }
        }

        if (numberBeginIndex < 0) {
            // Expecting a number but we didn't find any.
            throwStringFormatException();
        }

        if (numberEndIndex < 0) {
            // Handle number at the end of string.
            numberEndIndex = mString.length();
        }

        return Float.parseFloat(mString.substring(numberBeginIndex, numberEndIndex));
    }

    private void throwStringFormatException() {
        throw new IllegalArgumentException("Unsupported vector path format: " + mString);
    }
}
