package com.kotobyte.models;

public class Literal {

    private String mText;
    private Priority mPriority;

    public Literal(String text, Priority priority) {
        mText = text;
        mPriority = priority;
    }

    public String getText() {
        return mText;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public enum Priority {
        LOW, NORMAL, HIGH
    }
}
