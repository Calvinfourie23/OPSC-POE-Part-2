package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var addTaskButton: Button
    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var logHoursNavigationButton: Button
    private lateinit var accountsNavigationButton: Button
    private lateinit var goalDatePicker: DatePicker
    private lateinit var logsListView: ListView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize buttons
        addTaskButton = findViewById(R.id.addTaskButton)
        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)

        // Initialize views
        goalDatePicker = findViewById(R.id.goalDatePicker)
        logsListView = findViewById(R.id.listView)

        // Initialize Firebase Database reference for logs
        databaseReference = FirebaseDatabase.getInstance().getReference("timeLogs")

        // Set a default date for the DatePicker
        val today = Calendar.getInstance()
        goalDatePicker.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH),
            null
        )

        // Set click listeners
        addTaskButton.setOnClickListener { navigateToAddTask() }
        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        logHoursNavigationButton.setOnClickListener { navigateToLogHours() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }

        // Set listener for date picker
        goalDatePicker.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, dayOfMonth ->
            val selectedDate = formatDate(year, month, dayOfMonth)
            fetchLogsAndUpdateListView(selectedDate)
        }

        // Fetch logs and update ListView initially with today's date
        val todayDate = formatDate(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        fetchLogsAndUpdateListView(todayDate)
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun navigateToAddTask() {
        val intent = Intent(this, TasksActivity::class.java)
        startActivity(intent)
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

    private fun fetchLogsAndUpdateListView(selectedDate: String) {
        val calendar = Calendar.getInstance()
        calendar.time =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate) ?: return

        // Set start timestamp to the beginning of the selected date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTimestamp = calendar.timeInMillis

        // Set end timestamp to the end of the selected date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTimestamp = calendar.timeInMillis

        val logsQuery: Query = databaseReference.orderByChild("selectedDate")
            .startAt(startTimestamp.toString())
            .endAt(endTimestamp.toString())

        // Fetch daily goal from the database
        val dailyGoalQuery = FirebaseDatabase.getInstance().getReference("goals")
        dailyGoalQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val goalSnapshot = dataSnapshot.children.firstOrNull() // Assuming there's only one goal
                val goalDuration = goalSnapshot?.child("duration")?.getValue(Long::class.java) ?: 0L

                logsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(logsDataSnapshot: DataSnapshot) {
                        val logsList = mutableListOf<String>()
                        var totalMinutesLogged = 0L

                        for (logSnapshot in logsDataSnapshot.children) {
                            val log = logSnapshot.getValue(TimeLog::class.java)
                            log?.let {
                                val timeDifference =
                                    it.timeDifference.toLong() // Assuming timeDifference is in milliseconds
                                totalMinutesLogged += timeDifference / (1000 * 60) // Convert milliseconds to minutes
                            }
                        }

                        val percentage = if (goalDuration > 0) {
                            (totalMinutesLogged.toDouble() / goalDuration.toDouble()) * 100
                        } else {
                            0.0
                        }

                        logsList.add("Total Minutes Logged on $selectedDate: $totalMinutesLogged")
                        logsList.add("Percentage of Daily Goal: ${String.format("%.1f%%", percentage)}")

                        val adapter = ArrayAdapter(
                            this@DashboardActivity,
                            android.R.layout.simple_list_item_1,
                            logsList
                        )
                        logsListView.adapter = adapter
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}