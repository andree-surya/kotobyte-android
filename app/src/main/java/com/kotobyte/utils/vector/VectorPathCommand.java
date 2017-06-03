package com.kotobyte.utils.vector;

public abstract class VectorPathCommand {

    private boolean mIsRelative;

    private VectorPathCommand(boolean isRelative) {
        mIsRelative = isRelative;
    }

    public boolean isRelative() {
        return mIsRelative;
    }

    public static class Move extends VectorPathCommand {

        private float mX;
        private float mY;

        Move(float x, float y, boolean isRelative) {
            super(isRelative);

            mX = x;
            mY = y;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }
    }

    public static class Cubic extends VectorPathCommand {

        private float mX1;
        private float mY1;
        private float mX2;
        private float mY2;
        private float mX3;
        private float mY3;

        Cubic(float x1, float y1, float x2, float y2, float x3, float y3, boolean isRelative) {
            super(isRelative);

            mX1 = x1;
            mY1 = y1;
            mX2 = x2;
            mY2 = y2;
            mX3 = x3;
            mY3 = y3;
        }

        public float getX1() {
            return mX1;
        }

        public float getY1() {
            return mY1;
        }

        public float getX2() {
            return mX2;
        }

        public float getY2() {
            return mY2;
        }

        public float getX3() {
            return mX3;
        }

        public float getY3() {
            return mY3;
        }
    }
}
