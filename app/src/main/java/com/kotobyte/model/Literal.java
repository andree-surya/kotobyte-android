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

    public String getText() {
        return mText;
    }

    public Status getStatus() {
        return mStatus;
    }

    public enum Status {
        @SerializedName("common") COMMON,
        @SerializedName("irregular") IRREGULAR,
        @SerializedName("outdated") OUTDATED
    }
}
