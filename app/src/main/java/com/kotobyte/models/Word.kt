package com.kotobyte.models


data class Word(
        val ID: Long,
        val literals: List<Literal>,
        val readings: List<Literal>,
        val senses: List<Sense>
) {

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
}
