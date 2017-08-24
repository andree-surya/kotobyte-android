package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Kanji(
        val ID: Long,
        val character: Char,
        val readings: List<String>,
        val meanings: List<String>,
        val strokes: List<String>,
        val extras: List<String>

) : Parcelable {

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

    class Builder {

        private var ID: Long = 0
        private var character: Char = ' '
        private val readings = mutableListOf<String>()
        private val meanings = mutableListOf<String>()
        private val strokes = mutableListOf<String>()
        private val extras = mutableListOf<String>()

        fun setID(ID: Long) {
            this.ID = ID
        }

        fun setCharacter(character: Char) {
            this.character = character
        }

        fun addReadings(readings: Array<String>) {
            this.readings.addAll(readings)
        }

        fun addMeanings(meanings: Array<String>) {
            this.meanings.addAll(meanings)
        }

        fun addStrokes(strokes: Array<String>) {
            this.strokes.addAll(strokes)
        }

        fun addExtra(extra: String) {
            extras.add(extra)
        }

        fun build() = Kanji(
                ID,
                character,
                readings.toList(),
                meanings.toList(),
                strokes.toList(),
                extras.toList()
        )

        fun reset() {

            ID = 0
            character = ' '
            readings.clear()
            meanings.clear()
            readings.clear()
            strokes.clear()
            extras.clear()
        }
    }
}
