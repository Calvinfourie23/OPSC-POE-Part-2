package com.example.opsc7311poepart2

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.Manifest
import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Calendar

class LogHoursActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: FirebaseStorage
    private var imageUri: Uri? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102

    private lateinit var categoriesNavigationButton: Button
    private lateinit var tasksNavigationButton: Button
    private lateinit var timeSheetsNavigationButton: Button
    private lateinit var homeNavigationButton: ImageButton
    private lateinit var accountsNavigationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loghours)

        val spinnerTask = findViewById<Spinner>(R.id.spinnerTask)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val startTimePicker = findViewById<TimePicker>(R.id.startTimePicker)
        val endTimePicker = findViewById<TimePicker>(R.id.endTimePicker)
        val descriptionText = findViewById<EditText>(R.id.descriptionText)
        val addImageButton = findViewById<Button>(R.id.addImageButton)

        categoriesNavigationButton = findViewById(R.id.categoriesNavigationButton)
        tasksNavigationButton = findViewById(R.id.tasksNavigationButton)
        timeSheetsNavigationButton = findViewById(R.id.timeSheetsNavigationButton)
        homeNavigationButton = findViewById(R.id.homeNavigationButton)
        accountsNavigationButton = findViewById(R.id.accountsNavigationButton)

        // Initialize Firebase Database reference for categories
        val categoriesReference = FirebaseDatabase.getInstance().getReference("categories")

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
                    this@LogHoursActivity,
                    android.R.layout.simple_spinner_item,
                    categoriesList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTask.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        // Initialize Firebase Database reference for time logs
        databaseReference = FirebaseDatabase.getInstance().getReference("timeLogs")

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance()

        categoriesNavigationButton.setOnClickListener { navigateToCategories() }
        tasksNavigationButton.setOnClickListener { navigateToTasks() }
        timeSheetsNavigationButton.setOnClickListener { navigateToTimeSheets() }
        homeNavigationButton.setOnClickListener { navigateToHome() }
        accountsNavigationButton.setOnClickListener { navigateToAccounts() }

        // Button click listener to add a time log
        findViewById<Button>(R.id.logHoursButton).setOnClickListener {
            // Get selected category
            val selectedCategory = spinnerTask.selectedItem.toString()
            // Get selected date
            val calendar = Calendar.getInstance()
            calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val selectedDate = calendar.timeInMillis
            // Get start time
            val startHour = startTimePicker.hour
            val startMinute = startTimePicker.minute
            calendar.set(Calendar.HOUR_OF_DAY, startHour)
            calendar.set(Calendar.MINUTE, startMinute)
            val startTime = calendar.timeInMillis
            // Get end time
            val endHour = endTimePicker.hour
            val endMinute = endTimePicker.minute
            calendar.set(Calendar.HOUR_OF_DAY, endHour)
            calendar.set(Calendar.MINUTE, endMinute)
            val endTime = calendar.timeInMillis
            // Calculate time difference
            val timeDifference = endTime - startTime
            // Get description
            val description = descriptionText.text.toString()

            // Upload image to Firebase Storage if available
            imageUri?.let { uri ->
                val storageRef = storageReference.reference.child("images/${System.currentTimeMillis()}")
                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            // Save data to Firebase Realtime Database
                            val selectedDateStr = selectedDate.toString()
                            val timeDifferenceStr = timeDifference.toString()
                            val timeLogId = databaseReference.push().key
                            timeLogId?.let {
                                val timeLog = TimeLog(
                                    timeLogId,
                                    selectedDateStr,
                                    timeDifferenceStr,
                                    selectedCategory,
                                    description,
                                    imageUrl.toString()
                                )
                                databaseReference.child(it).setValue(timeLog)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle unsuccessful upload
                    }
            } ?: run {
                // Save data to Firebase Realtime Database without image
                val selectedDateStr = selectedDate.toString()
                val timeDifferenceStr = timeDifference.toString()
                val timeLogId = databaseReference.push().key
                timeLogId?.let {
                    val timeLog = TimeLog(
                        timeLogId,
                        selectedDateStr,
                        timeDifferenceStr,
                        selectedCategory,
                        description
                    )
                    databaseReference.child(it).setValue(timeLog)
                }
            }
        }

        // Button click listener to add image
        addImageButton.setOnClickListener {
            // Check if the camera permission is granted
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                // Request the camera permission
                requestCameraPermission()
            }
        }
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

    private fun navigateToHome() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAccounts() {
        val intent = Intent(this, AccountsActivity::class.java)
        startActivity(intent)
    }
    // Function to check if camera permission is granted
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Override onRequestPermissionsResult to handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the camera operation
                dispatchTakePictureIntent()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            if (checkWriteExternalStoragePermission()) {
                val imageFile = createImageFile()
                imageFile?.let {
                    val imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.opsc7311poepart2.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } else {
                requestWriteExternalStoragePermission()
            }
        }
    }

    private fun checkWriteExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "image_${System.currentTimeMillis()}",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Set the captured image to the ImageView
            findViewById<ImageView>(R.id.imageView1).setImageURI(imageUri)
            // Refresh the ImageView to display the new image
            findViewById<ImageView>(R.id.imageView1).invalidate()
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}