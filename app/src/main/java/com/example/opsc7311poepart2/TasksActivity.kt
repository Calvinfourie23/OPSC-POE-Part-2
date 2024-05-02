package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TasksActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var logHoursNavigationButton: Button
    private lateinit var accountsNavigationButton: Button
    private lateinit var homeNavigationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("goals")

        val addGoalButton = findViewById<Button>(R.id.addGoalButton)
        val goalNameEditText = findViewById<EditText>(R.id.etGoalName)
        val hourPicker = findViewById<EditText>(R.id.hourPicker)
        val minutePicker = findViewById<EditText>(R.id.minutePicker)

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)

// Set input filters to restrict input to numbers within the desired range
        hourPicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 23))
        minutePicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 59))

        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        logHoursNavigationButton.setOnClickListener { navigateToLogHours() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }
        homeNavigationButton.setOnClickListener{ navigateToHome() }

        // Button click listener to add a goal
        addGoalButton.setOnClickListener {
            // Get goal name
            val goalName = goalNameEditText.text.toString().trim()

            // Get duration from EditText fields
            val hourText = hourPicker.text.toString()
            val minuteText = minutePicker.text.toString()

            // Convert text to integers, default to 0 if parsing fails
            val hour = hourText.toIntOrNull() ?: 0
            val minute = minuteText.toIntOrNull() ?: 0
            val duration = hour * 60 + minute // Convert hours to minutes and add with minutes

            // Check if goal name is not empty and duration is greater than 0
            if (goalName.isNotEmpty() && duration > 0) {
                // Check if a goal already exists in the database
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val existingGoal = dataSnapshot.children.firstOrNull()?.getValue(Goal::class.java)

                        // If an existing goal is found, update its values
                        existingGoal?.let {
                            val updatedGoal = Goal(it.id!!, goalName, duration) // Ensure id is not nullable
                            databaseReference.child(it.id).setValue(updatedGoal)
                        } ?: run {
                            // If no existing goal is found, add a new goal
                            val goalId = databaseReference.push().key
                            goalId?.let {
                                val goal = Goal(it, goalName, duration)
                                databaseReference.child(it).setValue(goal)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database errors if necessary
                    }
                })

                // Clear input fields after adding or updating goal
                goalNameEditText.text.clear()
                // Set default values for EditText fields
                hourPicker.setText("0")
                minutePicker.setText("0")
            }
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