package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.university.marathononline.data.models.Rule
import com.university.marathononline.databinding.DialogAddRuleBinding
import java.time.LocalDateTime

class AddRuleDialog(
    context: Context,
    private val onRuleAdded: (Rule) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddRuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddRuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Save Rule button click listener
        binding.btnSaveRule.setOnClickListener {
            val ruleName = binding.etRuleName.text.toString().trim()
            val ruleDescription = binding.etRuleDescription.text.toString().trim()

            if (ruleName.isNotEmpty() && ruleDescription.isNotEmpty()) {
                val newRule = Rule(
                    id = System.currentTimeMillis(),
                    icon = "",
                    name = ruleName,
                    description = ruleDescription,
                    updateDate = LocalDateTime.now().toString()
                )
                onRuleAdded(newRule)
                dismiss()
            } else {
                // Show error if fields are empty
                if (ruleName.isEmpty()) binding.etRuleName.error = "Tên quy định là bắt buộc"
                if (ruleDescription.isEmpty()) binding.etRuleDescription.error = "Chi tiết quy định là bắt buộc"
            }
        }

        // Cancel button click listener
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}
