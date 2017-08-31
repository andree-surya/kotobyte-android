package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Word(
        val ID: Long,
        val literals: List<Literal>,
        val readings: List<Literal>,
        val senses: List<Sense>

) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(ID)
        parcel.writeTypedList(literals)
        parcel.writeTypedList(readings)
        parcel.writeTypedList(senses)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Word> {

        override fun createFromParcel(parcel: Parcel) = Word(
                parcel.readLong(),
                parcel.createTypedArrayList(Literal),
                parcel.createTypedArrayList(Literal),
                parcel.createTypedArrayList(Sense)
        )

        override fun newArray(size: Int): Array<Word?> = arrayOfNulls(size)
    }
}
