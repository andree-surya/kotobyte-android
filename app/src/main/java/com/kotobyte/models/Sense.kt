package com.kotobyte.models

data class Sense(
        val text: String,
        val categories: List<String>,
        val extras: List<String>,
        val origins: List<Origin>
) {

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
}
