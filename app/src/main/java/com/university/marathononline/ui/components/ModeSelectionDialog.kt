package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.ViewGroup
import com.university.marathononline.databinding.DialogModeSelectionBinding

class ModeSelectionDialog(
    context: Context,
    private val onNormalModeSelected: () -> Unit,
    private val onGuidedModeSelected: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogModeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DialogModeSelectionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Set the dialog width to match parent with margins
        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        setupListeners()
    }

    private fun setupListeners() {
        binding.cardNormalMode.setOnClickListener {
            onNormalModeSelected()
            dismiss()
        }

        binding.cardGuidedMode.setOnClickListener {
            onGuidedModeSelected()
            dismiss()
        }
    }
}