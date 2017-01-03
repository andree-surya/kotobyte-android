package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class Literal {

    @SerializedName("text")
    private String mText;

    @SerializedName("status")
    private Status mStatus;

    String getText() {
        return mText;
    }

    Status getStatus() {
        return mStatus;
    }

    enum Status {
        @SerializedName("common") COMMON,
        @SerializedName("irregular") IRREGULAR,
        @SerializedName("outdated") OUTDATED
    }
}
