package com.kotobyte.models;

import android.support.annotation.Nullable;

public class Origin {

    private String mLanguage;
    private String mText;

    public Origin(String language, String text) {
        mLanguage = language;
        mText = text;
    }

    public String getLanguage() {
        return mLanguage;
    }

    @Nullable
    public String getText() {
        return mText;
    }
}
