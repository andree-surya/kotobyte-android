package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Sense(
        val text: String,
        val categories: List<String>,
        val extras: List<String>,
        val origins: List<Origin>

) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeStringList(categories)
        parcel.writeStringList(extras)
        parcel.writeTypedList(origins)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Sense> {

        override fun createFromParcel(parcel: Parcel) = Sense(
                parcel.readString(),
                parcel.createStringArrayList(),
                parcel.createStringArrayList(),
                parcel.createTypedArrayList(Origin)
        )

        override fun newArray(size: Int): Array<Sense?> = arrayOfNulls(size)
    }
}
