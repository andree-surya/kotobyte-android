package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Origin(
        val language: String,
        val text: String?

) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(language)
        parcel.writeString(text)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Origin> {

        override fun createFromParcel(parcel: Parcel): Origin =
                Origin(parcel.readString(), parcel.readString())

        override fun newArray(size: Int): Array<Origin?> = arrayOfNulls(size)
    }

}
