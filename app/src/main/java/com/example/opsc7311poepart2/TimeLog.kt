package com.example.opsc7311poepart2

data class TimeLog(
    val timeLogId: String = "",
    val selectedDate: String = "",
    val timeDifference: String = "",
    val selectedCategory: String = "",
    val description: String = "",
    val imageUrl: String? = null // Optional image URL
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", "",null)
}