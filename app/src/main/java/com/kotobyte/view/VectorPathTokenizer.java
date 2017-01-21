package com.kotobyte.view;

/**
 * Created by andree.surya on 2017/01/21.
 */
class VectorPathTokenizer {

    private String mPath = null;
    private int mOffset = 0;

    private StringBuilder mFloatStringBuilder = new StringBuilder();

    public void setString(String path) {
        this.mPath = path.trim();
        this.mOffset = 0;
    }

    boolean hasNext() {
        return mOffset < mPath.length();
    }

    char nextChar() {
        char c = mPath.charAt(mOffset);

        mOffset += 1;

        return c;
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
}
