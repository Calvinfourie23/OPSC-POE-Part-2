package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AccountsActivity : AppCompatActivity() {

    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var logHoursNavigationButton: Button
    private lateinit var accountsNavigationButton: Button
    private lateinit var homeNavigationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accounts)

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)

        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        logHoursNavigationButton.setOnClickListener { navigateToLogHours() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }
        homeNavigationButton.setOnClickListener{ navigateToHome() }
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