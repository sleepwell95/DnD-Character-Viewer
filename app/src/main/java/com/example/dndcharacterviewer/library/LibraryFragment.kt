package com.example.dndcharacterviewer.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dndcharacterviewer.databinding.FragmentLibraryBinding
import com.example.dndcharacterviewer.databinding.ItemCharacterBinding
import com.example.dndcharacterviewer.data.CharacterStore
import com.example.dndcharacterviewer.data.CharacterSummary
import com.example.dndcharacterviewer.R
import com.google.android.material.snackbar.Snackbar

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var store: CharacterStore
    private val adapter = CharacterAdapter(::onCharacterClick)

    // SAF multiple-file picker
    private val importPicker = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                runCatching { store.importFrom(uri) }
                    .onFailure {
                        Snackbar.make(binding.root, "Import failed", Snackbar.LENGTH_LONG).show()
                    }
            }
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        store = CharacterStore(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recycler.adapter = adapter

        // Empty-state import button
        binding.emptyImport.setOnClickListener {
            Log.d("Library", "Import JSON (empty state) clicked")
            openPicker()
        }

        // NEW: "+" FAB import (only visible when list is not empty; visibility toggled in refresh)
        binding.addFab.setOnClickListener {
            Log.d("Library", "Import JSON (FAB) clicked")
            openPicker()
        }

        refresh()
    }

    private fun openPicker() {
        importPicker.launch(arrayOf("application/json"))
    }

    /** Refresh the visible list and show/hide empty state & FAB properly */
    private fun refresh() {
        val items = store.list()
        adapter.submitList(items)

        val isEmpty = items.isEmpty()
        binding.recycler.visibility   = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.addFab.visibility     = if (isEmpty) View.GONE else View.VISIBLE // show "+" only after first import
    }

    private fun onCharacterClick(item: CharacterSummary) {
        Snackbar.make(binding.root, "Open ${item.name}", Snackbar.LENGTH_SHORT).show()
        // Later: navigate to FirstFragment with selected file path
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_library, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_import -> { openPicker(); true }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/* ---------------- RecyclerView adapter below ---------------- */

private class CharacterAdapter(
    val onClick: (CharacterSummary) -> Unit
) : ListAdapter<CharacterSummary, CharacterVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterVH {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterVH(binding, onClick)
    }

    override fun onBindViewHolder(holder: CharacterVH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CharacterSummary>() {
            override fun areItemsTheSame(o: CharacterSummary, n: CharacterSummary) = o.id == n.id
            override fun areContentsTheSame(o: CharacterSummary, n: CharacterSummary) = o == n
        }
    }
}

private class CharacterVH(
    private val binding: ItemCharacterBinding,
    private val onClick: (CharacterSummary) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CharacterSummary) = with(binding) {
        title.text = item.name
        subtitle.text = buildString {
            item.clazz?.let { append(it) }
            item.level?.let {
                if (isNotEmpty()) append(" • ")
                append("Lv $it")
            }
        }.ifBlank { "—" }
        root.setOnClickListener { onClick(item) }
    }
}
