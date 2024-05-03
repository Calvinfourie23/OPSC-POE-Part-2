package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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

    private lateinit var scrollView: ScrollView
    private lateinit var goalsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("goals")

        val addGoalButton = findViewById<Button>(R.id.addGoalButton)
        val goalNameEditText = findViewById<EditText>(R.id.etGoalName)
        val minHourPicker = findViewById<EditText>(R.id.minHourPicker)
        val minMinutePicker = findViewById<EditText>(R.id.minMinutePicker)
        val maxHourPicker = findViewById<EditText>(R.id.maxHourPicker)
        val maxMinutePicker = findViewById<EditText>(R.id.maxMinutePicker)

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)
        scrollView = findViewById(R.id.scrollViewGoals)
        goalsContainer = findViewById(R.id.goalsContainer)

        minHourPicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 23))
        maxHourPicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 23))
        minMinutePicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 59))
        maxMinutePicker.filters = arrayOf(InputFilter.LengthFilter(2), RangeInputFilter(0, 59))

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
            val minHourText = minHourPicker.text.toString()
            val maxHourText = maxHourPicker.text.toString()
            val minMinuteText = minMinutePicker.text.toString()
            val maxMinuteText = maxMinutePicker.text.toString()

            // Convert text to integers, default to 0 if parsing fails
            val minHour = minHourText.toIntOrNull() ?: 0
            val maxHour = maxHourText.toIntOrNull() ?: 0
            val minMinute = minMinuteText.toIntOrNull() ?: 0
            val maxMinute = maxMinuteText.toIntOrNull() ?: 0

            val minDuration = minHour * 60 + minMinute
            val maxDuration = maxHour * 60 + maxMinute

            // Check if goal name is not empty and duration is greater than 0
            if (goalName.isNotEmpty() && minDuration > 0 && maxDuration > 0 && minDuration <= maxDuration) {
                // Check if a goal already exists in the database
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val existingGoal = dataSnapshot.children.firstOrNull()?.getValue(Goal::class.java)

                        // If an existing goal is found, update its values
                        existingGoal?.let {
                            val updatedGoal = Goal(it.id!!, goalName, minDuration, maxDuration) // Ensure id is not nullable
                            databaseReference.child(it.id).setValue(updatedGoal)
                        } ?: run {
                            // If no existing goal is found, add a new goal
                            val goalId = databaseReference.push().key
                            goalId?.let {
                                val goal = Goal(it, goalName, minDuration, maxDuration)
                                databaseReference.child(it).setValue(goal)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        
                    }
                })

                // Clear input fields after adding or updating goal
                goalNameEditText.text.clear()
                minHourPicker.text.clear()
                maxHourPicker.text.clear()
                minMinutePicker.text.clear()
                maxMinutePicker.text.clear()
            }
        }
        retrieveAndDisplayGoals()
    }

    private fun retrieveAndDisplayGoals() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                goalsContainer.removeAllViews() // Clear existing views

                for (goalSnapshot in dataSnapshot.children) {
                    val goal = goalSnapshot.getValue(Goal::class.java)
                    goal?.let {
                        val goalView = layoutInflater.inflate(R.layout.goal_item, null)
                        val goalNameTextView = goalView.findViewById<TextView>(R.id.goalNameTextView)
                        val minDurationTextView = goalView.findViewById<TextView>(R.id.minDurationTextView)
                        val maxDurationTextView = goalView.findViewById<TextView>(R.id.maxDurationTextView)

                        goalNameTextView.text = it.name
                        minDurationTextView.text = "Min: ${it.minDuration/60} Hours and ${it.minDuration%60} Minutes"
                        maxDurationTextView.text = "Max: ${it.maxDuration/60} Hours and ${it.maxDuration%60} Minutes"

                        goalsContainer.addView(goalView)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
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