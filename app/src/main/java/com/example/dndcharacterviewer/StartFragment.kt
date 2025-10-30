package com.example.dndcharacterviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dndcharacterviewer.databinding.StartScreenBinding

class StartFragment : Fragment() {

    private var _binding: StartScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StartScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        showEmptyState(true)

        binding.importButton.setOnClickListener {
            // Placeholder: hook up to file picker/import flow when available
        }
    }

    private fun setupRecyclerView() {
        binding.characterList.layoutManager = LinearLayoutManager(requireContext())
        // Adapter will be provided when the data source is ready
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyStateGroup.isVisible = isEmpty
        binding.characterList.isVisible = !isEmpty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
