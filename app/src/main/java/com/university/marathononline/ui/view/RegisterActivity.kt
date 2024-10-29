package com.university.marathononline.ui.view

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.text.InputType
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.university.marathononline.R
import com.university.marathononline.databinding.ActivityRegisterBinding
import android.widget.EditText

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false

    private fun setupPasswordToggle(editText: EditText, drawableVisible: Int, drawableHidden: Int) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editText.right - editText.compoundDrawables[2].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    editText.inputType = if (isPasswordVisible) {
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isPasswordVisible) drawableVisible else drawableHidden, 0)
                    editText.setSelection(editText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginText = findViewById<TextView>(R.id.loginText)

        binding.registerPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)

        setupPasswordToggle(binding.registerPasswordEditText, R.drawable.password_visible_off_icon, R.drawable.password_icon)
        setupPasswordToggle(binding.registerConfirmPasswordEditText, R.drawable.password_visible_off_icon, R.drawable.password_icon)

        ViewCompat.setOnApplyWindowInsetsListener(binding.register) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginText.setOnClickListener {
            // Xử lý sự kiện nhấn vào "Login"
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}