package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable


data class Word(
        val ID: Long,
        val literals: List<Literal>,
        val readings: List<Literal>,
        val senses: List<Sense>

) : Parcelable {

    class Builder {

        private var ID: Long? = null
        private var literals: List<Literal>? = null
        private var readings: List<Literal>? = null
        private var senses: List<Sense>? = null

        fun setID(ID: Long) {
            this.ID = ID
        }

        fun setLiterals(literals: List<Literal>?) {
            this.literals = literals
        }

        fun setReadings(readings: List<Literal>?) {
            this.readings = readings
        }

        fun setSenses(senses: List<Sense>?) {
            this.senses = senses
        }

        fun build() = Word(
                ID ?: 0,
                literals?.toList() ?: listOf(),
                readings?.toList() ?: listOf(),
                senses?.toList() ?: listOf()
        )

        fun reset() {
            ID = null
            literals = null
            readings = null
            senses = null
        }
    }

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
