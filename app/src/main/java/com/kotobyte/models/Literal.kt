package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Literal(
        val text: String,
        val priority: Priority

) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeInt(priority.ordinal)
    }

    override fun describeContents(): Int = 0

    enum class Priority {
        LOW, NORMAL, HIGH
    }

    companion object CREATOR : Parcelable.Creator<Literal> {

        override fun createFromParcel(parcel: Parcel): Literal =
                Literal(parcel.readString(), Priority.values()[parcel.readInt()])

        override fun newArray(size: Int): Array<Literal?> = arrayOfNulls(size)
    }
}
