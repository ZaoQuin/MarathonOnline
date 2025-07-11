package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.university.marathononline.data.models.Rule
import com.university.marathononline.databinding.DialogAddRuleBinding
import java.time.LocalDateTime

class AddRuleDialog(
    context: Context,
    private val onRuleAdded: (Rule) -> Unit,
    private val onRuleUpdated: (Rule) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddRuleBinding
    private var ruleToEdit: Rule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogAddRuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the save button click listener
        binding.btnSaveRule.setOnClickListener {
            val ruleName = binding.etRuleName.text.toString().trim()
            val ruleDescription = binding.etRuleDescription.text.toString().trim()

            if (ruleName.isNotEmpty() && ruleDescription.isNotEmpty()) {

                if (ruleToEdit == null) {

                    val newRule = Rule(
                        id = System.currentTimeMillis(),
                        icon = "",
                        name = ruleName,
                        description = ruleDescription,
                        updateDate = LocalDateTime.now().toString()
                    )
                    Log.d("Update Dlog", "Mé Add")
                    onRuleAdded(newRule)
                } else {

                    val newRule = Rule(
                        id = ruleToEdit!!.id,
                        icon = ruleToEdit!!.icon,
                        name = ruleName,
                        description = ruleDescription,
                        updateDate = LocalDateTime.now().toString()
                    )
                    Log.d("Update Dlog", "Mé")
                    ruleToEdit = null
                    onRuleUpdated(newRule) // If editing, call onRuleUpdated
                }

                dismiss()
            } else {
                if (ruleName.isEmpty()) binding.etRuleName.error = "Tên quy tắc là bắt buộc"
                if (ruleDescription.isEmpty()) binding.etRuleDescription.error = "Mô tả quy tắc là bắt buộc"
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        ruleToEdit?.let {
            setRule(it)
        }
    }

    fun setRule(rule: Rule) {
        ruleToEdit = rule
        if (this::binding.isInitialized) {
            binding.etRuleName.setText(rule.name)
            binding.etRuleDescription.setText(rule.description)
        }
    }
}

