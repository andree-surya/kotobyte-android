package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Kanji(
        override val ID: Long,
        val character: Char,
        val readings: List<String>,
        val meanings: List<String>,
        val strokes: List<String>,
        val extras: List<String>

) : Entry, Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(ID)
        parcel.writeInt(character.toInt())
        parcel.writeStringList(readings)
        parcel.writeStringList(meanings)
        parcel.writeStringList(strokes)
        parcel.writeStringList(extras)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Kanji> {

        override fun createFromParcel(parcel: Parcel) = Kanji(
                parcel.readLong(),
                parcel.readInt().toChar(),
                parcel.createStringArrayList(),
                parcel.createStringArrayList(),
                parcel.createStringArrayList(),
                parcel.createStringArrayList()
        )

        override fun newArray(size: Int): Array<Kanji?> = arrayOfNulls(size)
    }
}
