package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.databinding.FragmentNotifyBinding
import com.university.marathononline.data.models.Notify
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.ui.adapter.NotifyAdapter

class NotifyFragment : Fragment() {

    private lateinit var binding: FragmentNotifyBinding
    private val viewModel: NotifyViewModel by viewModels()
    private lateinit var adapter: NotifyAdapter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notifies.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotifyAdapter(emptyList())
        binding.notifies.adapter = adapter

        observe()
    }

    private fun observe() {
        viewModel.notifies.observe(viewLifecycleOwner) { notifies: List<Notify> ->
            adapter.updateData(notifies)
        }
    }
}