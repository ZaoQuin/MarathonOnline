package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.university.marathononline.R
import com.university.marathononline.data.models.Rule

class AddRuleDialog(
    context: Context,
    private val onRuleAdded: (Rule) -> Unit
) : Dialog(context) {

    private lateinit var etRuleName: EditText
    private lateinit var etRuleDescription: EditText
    private lateinit var btnSaveRule: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_rule)

        // Initialize views
        etRuleName = findViewById(R.id.etRuleName)
        etRuleDescription = findViewById(R.id.etRuleDescription)
        btnSaveRule = findViewById(R.id.btnSaveRule)
        btnCancel = findViewById(R.id.btnCancel)

        // Save Rule button click listener
        btnSaveRule.setOnClickListener {
            val ruleName = etRuleName.text.toString().trim()
            val ruleDescription = etRuleDescription.text.toString().trim()

            if (ruleName.isNotEmpty() && ruleDescription.isNotEmpty()) {
//                val newRule = Rule(
//                    id = System.currentTimeMillis(), // Temporary ID generation
//                    name = ruleName,
//                    description = ruleDescription
//                )
//                onRuleAdded(newRule)
                dismiss()
            } else {
                // Show error if fields are empty
                if (ruleName.isEmpty()) etRuleName.error = "Rule name is required"
                if (ruleDescription.isEmpty()) etRuleDescription.error = "Rule description is required"
            }
        }

        // Cancel button click listener
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}