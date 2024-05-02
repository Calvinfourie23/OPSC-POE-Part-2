package com.example.opsc7311poepart2

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.Manifest
import android.app.Activity
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
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

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

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    private val TAG = "LogHoursActivity"

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
                    .addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            // Save data to Firebase Realtime Database
                            val selectedDateStr = selectedDate.toString()
                            val timeDifferenceStr = timeDifference.toString()
                            val imageUrlString = imageUrl.toString() // Obtain the download URL of the image
                            val timeLogId = databaseReference.push().key
                            timeLogId?.let {
                                val timeLog = TimeLog(
                                    timeLogId,
                                    selectedDateStr,
                                    timeDifferenceStr,
                                    selectedCategory,
                                    description,
                                    imageUrlString
                                )
                                databaseReference.child(it).setValue(timeLog)
                                    .addOnSuccessListener {
                                        // Data saved successfully
                                        Toast.makeText(this, "Time log added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle errors
                                        Log.e(TAG, "Error adding time log to database: $e")
                                        Toast.makeText(this, "Failed to add time log", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle unsuccessful upload
                        Log.e(TAG, "Error uploading image: $exception")
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }?: run {
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

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val imageView = findViewById<ImageView>(R.id.imageView1)
                imageView.setImageURI(imageUri)
                imageView.invalidate()
            } else {
                // Handle the case where the user cancels or the operation fails
                Toast.makeText(this, "Camera operation canceled", Toast.LENGTH_SHORT).show()
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

    private fun dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent: Starting camera intent")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            if (checkWriteExternalStoragePermission()) {
                val imageFile = createImageFile()
                imageFile?.let {
                    imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.opsc7311poepart2.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    takePictureLauncher.launch(takePictureIntent)
                }
            } else {
                Log.d(TAG, "dispatchTakePictureIntent: Write external storage permission not granted")
                requestWriteExternalStoragePermission()
            }
        } else {
            Log.d(TAG, "dispatchTakePictureIntent: No camera app found")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Camera permission granted, starting camera intent")
                dispatchTakePictureIntent()
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Camera permission denied")
                // Permission denied, handle accordingly (e.g., show a message)
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