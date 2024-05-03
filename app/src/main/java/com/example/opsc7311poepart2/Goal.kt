package com.example.opsc7311poepart2

data class Goal(
    val id: String? = null,
    val name: String = "",
    val duration: Int = 0, // Duration in minutes
    val maxDuration: Int
)