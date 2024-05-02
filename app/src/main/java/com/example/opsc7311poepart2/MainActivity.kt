package com.example.opsc7311poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        // Set click listeners
        loginButton.setOnClickListener { attemptLogin() }
        registerButton.setOnClickListener { /* Handle registration */ }
    }

    private fun attemptLogin() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        // Perform validation (you may want to improve this)
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Implement your login logic here (e.g., validate credentials)
        // For demo purpose, let's assume successful login
        if (username == "demo" && password == "demo") {
            // Navigate to the next screen
            navigateToNextScreen()
        } else {
            // Show error message for invalid credentials
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish() // Prevent user from coming back to login screen using back button
    }
}