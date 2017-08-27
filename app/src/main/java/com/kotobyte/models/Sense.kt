package com.kotobyte.models

import android.os.Parcel
import android.os.Parcelable

data class Sense(
        val text: String,
        val categories: List<String>,
        val extras: List<String>,
        val origins: List<Origin>

) : Parcelable {

    class Builder {

        private var text: String? = null
        private var categories: List<String>? = null
        private var origins: List<Origin>? = null
        private val extras = mutableListOf<String>()

        fun setText(text: String) {
            this.text = text
        }

        fun setCategories(categories: List<String>?) {
            this.categories = categories
        }

        fun addExtras(extras: List<String>?) {

            if (extras != null) {
                this.extras.addAll(extras)
            }
        }

        fun setOrigins(origins: List<Origin>?) {
            this.origins = origins
        }

        fun build() = Sense(
                text ?: "",
                categories?.toList() ?: listOf(),
                extras.toList(),
                origins?.toList() ?: listOf()
        )

        fun reset() {
            text = null
            categories = null
            origins = null
            extras.clear()
        }
    }

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
