package com.kotobyte.utils;


class VectorPathTokenizer {

    private String mPath = null;
    private int mOffset = 0;
    private char mLastCommand = 0;

    private StringBuilder mFloatStringBuilder = new StringBuilder();

    public void setString(String path) {
        this.mPath = path.trim();
        this.mOffset = 0;
    }

    char nextCommand() {

        for (int i = mOffset; i < mPath.length(); i++) {
            char c = mPath.charAt(i);

            if (Character.isLetter(c)) {
                mOffset = i + 1;
                mLastCommand = c;

                return c;
            }

            if (! Character.isWhitespace(c)) {
                mOffset = i;

                return mLastCommand;
            }
        }

        return 0;
    }

    float nextFloat() {
        mFloatStringBuilder.setLength(0);

        do {
            char c = nextChar();

            if (mFloatStringBuilder.length() == 0 && (Character.isSpaceChar(c) || c == ',')) {
                continue; // Skip leading spaces and commas.
            }

            if ((mFloatStringBuilder.length() == 0 && c == '-') || Character.isDigit(c) || c == '.') {
                mFloatStringBuilder.append(c); // Valid floating point character.

            } else { // No more floating point character. Take a step back and stop processing.
                mOffset -= 1;

                break;
            }

        } while (hasNext());

        return Float.parseFloat(mFloatStringBuilder.toString());
    }

    private char nextChar() {
        char c = mPath.charAt(mOffset);

        mOffset += 1;

        return c;
    }

    private boolean hasNext() {
        return mOffset < mPath.length();
    }
}
