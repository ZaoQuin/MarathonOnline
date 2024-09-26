package com.university.marathononline.notify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.university.marathononline.databinding.FragmentNotifyBinding

class NotifyFragment : Fragment() {

    private lateinit var binding: FragmentNotifyBinding
    private val viewModel: NotifyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotifyBinding.inflate(inflater, container, false)
        return binding.root
    }
}