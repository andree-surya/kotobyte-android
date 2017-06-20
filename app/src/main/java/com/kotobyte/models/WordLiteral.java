package com.kotobyte.models;

import android.support.annotation.Nullable;

public class WordLiteral {

    private String mText;
    private Status mStatus;

    public WordLiteral(String text, Status status) {
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
