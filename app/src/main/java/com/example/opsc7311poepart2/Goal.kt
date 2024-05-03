package com.example.opsc7311poepart2

data class Goal(
    val id: String? = null,
    val name: String = "",
    val minDuration: Int = 0, // Minimum duration in minutes
    val maxDuration: Int = 0 // Maximum duration in minutes
)
