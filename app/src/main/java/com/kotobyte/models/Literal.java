package com.kotobyte.models;

import android.support.annotation.Nullable;

public class Literal {

    private String mText;
    private Status mStatus;

    public Literal(String text, Status status) {
        mText = text;
        mStatus = status;
    }

    public String getText() {
        return mText;
    }

    @Nullable
    public Status getStatus() {
        return mStatus;
    }

    public enum Status {
        COMMON, IRREGULAR
    }
}
