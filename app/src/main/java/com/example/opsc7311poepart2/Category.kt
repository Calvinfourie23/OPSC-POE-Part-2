package com.example.opsc7311poepart2

data class Category(val name: String, val description: String, val duration: String, val priorityLevel: String)

object CategoryManager {
    private val categories: MutableList<Category> = mutableListOf()

    // Function to add a category
    fun addCategory(category: Category) {
        categories.add(category)
    }

    // Function to remove a category
    fun removeCategory(category: Category) {
        categories.remove(category)
    }

    // Function to get all categories
    fun getCategories(): List<Category> {
        return categories.toList()
    }
}