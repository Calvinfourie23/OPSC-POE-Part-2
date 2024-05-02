package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class CategoriesActivity : AppCompatActivity() {
    private lateinit var categoryText: EditText
    private lateinit var addCategoryButton: Button
    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var logHoursNavigationButton: Button
    private lateinit var accountsNavigationButton: Button
    private lateinit var homeNavigationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.categories)

        // Initialize views
        categoryText = findViewById(R.id.categoryText)
        addCategoryButton = findViewById(R.id.addCategoryButton)

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)

        // Set click listener for the add category button
        addCategoryButton.setOnClickListener {
            val categoryName = categoryText.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                addCategoryToFirebase(categoryName)
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        logHoursNavigationButton.setOnClickListener { navigateToLogHours() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }
        homeNavigationButton.setOnClickListener{ navigateToHome() }


    }
    private fun addCategoryToFirebase(categoryName: String) {
        val categoriesRef = FirebaseDatabase.getInstance().getReference("categories")
        val categoryKey = categoriesRef.push().key // Generate a unique key for the category
        if (categoryKey != null) {
            categoriesRef.child(categoryKey).setValue(categoryName)
                .addOnSuccessListener {
                    Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Failed to generate category key", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCategories() {

        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToTasks() {

        val intent = Intent(this, TasksActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToTimeSheets() {

        val intent = Intent(this, TimeSheetsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogHours() {

        val intent = Intent(this, LogHoursActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAccounts() {

        val intent = Intent(this, AccountsActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToHome() {

        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}