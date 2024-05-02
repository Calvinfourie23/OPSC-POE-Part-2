package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TimeSheetsActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var logRecyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private var logsList: MutableList<TimeLog> = mutableListOf()
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private lateinit var categoriesSpinner: Spinner
    private lateinit var textView: TextView
    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var logHoursNavigationButton: Button
    private lateinit var accountsNavigationButton: Button
    private lateinit var homeNavigationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timesheets)

        // Initialize Firebase Database reference for categories
        val categoriesReference = FirebaseDatabase.getInstance().getReference("categories")

        // Initialize views
        categoriesSpinner = findViewById(R.id.categoriesSpinner)
        textView = findViewById(R.id.textView) // Initialize textView here

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        logHoursNavigationButton = findViewById(R.id.logHoursNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)


        // Listen for changes in the categories data
        categoriesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categoriesList = mutableListOf<String>()
                for (categorySnapshot in dataSnapshot.children) {
                    val categoryName = categorySnapshot.getValue(String::class.java)
                    categoryName?.let {
                        categoriesList.add(it)
                    }
                }
                // Populate spinner with categories
                val adapter = ArrayAdapter(
                    this@TimeSheetsActivity,
                    android.R.layout.simple_spinner_item,
                    categoriesList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        // Initialize Firebase Database reference for logs
        databaseReference = FirebaseDatabase.getInstance().getReference("timeLogs")

        // Initialize RecyclerView and adapter
        logRecyclerView = findViewById(R.id.logRecyclerView)
        logRecyclerView.layoutManager = LinearLayoutManager(this)
        logAdapter = LogAdapter(this, logsList)
        logRecyclerView.adapter = logAdapter

        // Set up start date picker
        val startDatePicker = findViewById<DatePicker>(R.id.startDatePicker)
        startDatePicker.init(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            selectedStartDate = formatDate(year, monthOfYear, dayOfMonth)
            updateLogs(selectedStartDate, selectedEndDate)
        }

        // Set up end date picker
        val endDatePicker = findViewById<DatePicker>(R.id.endDatePicker)
        endDatePicker.init(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            selectedEndDate = formatDate(year, monthOfYear, dayOfMonth)
            updateLogs(selectedStartDate, selectedEndDate)
        }

        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        logHoursNavigationButton.setOnClickListener { navigateToLogHours() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }
        homeNavigationButton.setOnClickListener{ navigateToHome() }

        // Set up spinner listener
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateLogs(selectedStartDate, selectedEndDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected
            }
        }

        // Initial load of logs
        selectedStartDate = formatDate(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
        selectedEndDate = formatDate(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
        updateLogs(selectedStartDate, selectedEndDate)
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

    // Function to format date
    private fun formatDate(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, monthOfYear, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // Function to update logs based on selected date range
    private fun updateLogs(selectedStartDate: String, selectedEndDate: String) {
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedStartDate)
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedEndDate)
        val selectedCategory = categoriesSpinner.selectedItem?.toString() ?: ""

        // Initialize variables to hold total hours and minutes
        var totalHours = 0
        var totalMinutes = 0

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                logsList.clear() // Clear previous data
                for (logSnapshot in dataSnapshot.children) {
                    val log = logSnapshot.getValue(TimeLog::class.java)
                    log?.let {
                        val logDate = Date(it.selectedDate.toLong())
                        if (logDate in startDate..endDate && it.selectedCategory == selectedCategory) {
                            // Calculate total time
                            totalHours += it.timeDifference.toInt() / 3600000 // Convert milliseconds to hours
                            totalMinutes += (it.timeDifference.toInt() % 3600000) / 60000 // Convert remaining milliseconds to minutes

                            // Add log to the list
                            logsList.add(it)
                        }
                    }
                }

                // Update RecyclerView adapter
                logAdapter.notifyDataSetChanged()

                // Display total time in TextView
                textView.text = "Total Time: $totalHours hours $totalMinutes minutes"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}