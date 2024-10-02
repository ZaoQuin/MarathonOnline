package com.university.marathononline.login

import com.university.marathononline.entity.User
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.university.marathononline.MainActivity
import com.university.marathononline.R
import com.university.marathononline.databinding.ActivityLoginBinding
import com.university.marathononline.databinding.ActivityMainBinding
import com.university.marathononline.record.RecordActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    // Tạo đối tượng User
    private val user = User(
        id = 1L,
        fullName = "Nguyen Van A",
        phoneNumber = "0123456789",
        email = "nguyenvana@example.com",
        gender = "Male",
        username = "nguyenvana",
        password = "password123"
    )

    private lateinit var passwordEditText: EditText
    private lateinit var usernameEditText: EditText

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            val intent = Intent(binding.root.context, MainActivity::class.java)
            binding.root.context.startActivity(intent)
        }
//
//        passwordEditText = findViewById(R.id.passwordEditText)
//        usernameEditText = findViewById(R.id.usernameEditText)
//        val loginButton: Button = findViewById(R.id.loginButton)
//
//        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)
//
//        passwordEditText.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[2].bounds.width())) {
//                    isPasswordVisible = !isPasswordVisible
//                    if (isPasswordVisible) {
//                        passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                            R.drawable.password_visible_off_icon, 0)
//                    } else {
//                        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                            R.drawable.password_icon, 0)
//                    }
//                    passwordEditText.setSelection(passwordEditText.text.length)
//                    return@setOnTouchListener true
//                }
//            }
//            false
//        }

//        binding.loginButton.setOnClickListener {
//            val usernameInput = usernameEditText.text.toString()
//            val passwordInput = passwordEditText.text.toString()
//
//            if (usernameInput == user.username && passwordInput == user.password) {
//                val intent = Intent(this, MainActivity::class.java)
//                intent.putExtra("mockData", "This is some mock data")
//                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show()
//            }
//        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}