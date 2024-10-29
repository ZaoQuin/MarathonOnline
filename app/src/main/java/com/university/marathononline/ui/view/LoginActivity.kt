package com.university.marathononline.ui.view

import com.university.marathononline.data.models.User
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.university.marathononline.R
import com.university.marathononline.databinding.ActivityLoginBinding
import java.util.Date
import android.widget.TextView

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    // Tạo đối tượng User
    private val user = User(
        id = 1L,
        fullName = "Nguyen Van A",
        phoneNumber = "0123456789",
        birthday = Date(),
        email = "nguyenvana@example.com",
        gender = "Male",
        username = "nguyenvana",
        password = "password123"
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signUpText = findViewById<TextView>(R.id.signUpText)

        binding.loginButton.setOnClickListener {
            val usernameInput = binding.usernameEditText.text.toString()
            val passwordInput = binding.passwordEditText.text.toString()

            if (usernameInput == user.username && passwordInput == user.password) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("mockData", "This is some mock data")
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)

        binding.passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.passwordEditText.right - binding.passwordEditText.compoundDrawables[2].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_visible_off_icon, 0)
                    } else {
                        binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)
                    }
                    binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.login) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signUpText.setOnClickListener {
            // Xử lý sự kiện nhấn vào "Sign up"
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}