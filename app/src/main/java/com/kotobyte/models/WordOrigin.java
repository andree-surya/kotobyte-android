package com.kotobyte.models;

import android.support.annotation.Nullable;

public class WordOrigin {

    private String mLanguageCode;
    private String mText;

    public WordOrigin(String languageCode, String text) {
        mLanguageCode = languageCode;
        mText = text;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    @Nullable
    public String getText() {
        return mText;
    }
}
