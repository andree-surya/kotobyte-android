package com.kotobyte.models

data class Literal(val text: String, val priority: Priority) {

    enum class Priority {
        LOW, NORMAL, HIGH
    }
}
