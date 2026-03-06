package com.zaky.ruangku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.zaky.ruangku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: NotesAdapter
    
    private var selectedCategory: String? = null
    private var selectedPriority: String? = null
    private var selectedStatus: String? = null
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menangani Insets agar toolbar mepet ke atas
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        
        dbHelper = DatabaseHelper(this)

        adapter = NotesAdapter(mutableListOf(), onClick = { notes ->
            val intent = Intent(this, AddEditActivity::class.java)
            intent.putExtra("EXTRA_NOTE", notes)
            startActivity(intent)
        }, onDelete = { notes ->
            showDeleteDialog(notes)
        })

        binding.rcCatatan.layoutManager = LinearLayoutManager(this)
        binding.rcCatatan.adapter = adapter

        binding.rcCatatan.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && binding.fabAdd.isShown) {
                    binding.fabAdd.hide()
                } else if (dy < 0 && !binding.fabAdd.isShown) {
                    binding.fabAdd.show()
                }
            }
        })

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditActivity::class.java)
            startActivity(intent)
        }

        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        binding.btnSettings.setOnClickListener {
            showSettingsBottomSheet()
        }

        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                loadData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        animateFabPopIn()
    }

    private fun showSettingsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_settings_bottom_sheet, null)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_filter_bottom_sheet, null)
        dialog.setContentView(view)

        val cgKategori = view.findViewById<ChipGroup>(R.id.cgKategori)
        val cgPrioritas = view.findViewById<ChipGroup>(R.id.cgPrioritas)
        val cgStatus = view.findViewById<ChipGroup>(R.id.cgStatus)
        val btnApply = view.findViewById<Button>(R.id.btnApply)
        val btnReset = view.findViewById<Button>(R.id.btnReset)

        val allNotes = dbHelper.getAllData()
        
        val categories = (Notes.PILIHAN_KATEGORI.toList() + allNotes.map { it.kategori }).distinct().filter { it.isNotEmpty() }
        val priorities = (Notes.PILIHAN_PRIORITAS.toList() + allNotes.map { it.prioritas }).distinct().filter { it.isNotEmpty() }
        val statuses = (Notes.PILIHAN_STATUS.toList() + allNotes.map { it.status }).distinct().filter { it.isNotEmpty() }

        populateChipGroup(cgKategori, categories, selectedCategory)
        populateChipGroup(cgPrioritas, priorities, selectedPriority)
        populateChipGroup(cgStatus, statuses, selectedStatus)

        btnReset.setOnClickListener {
            selectedCategory = null
            selectedPriority = null
            selectedStatus = null
            loadData()
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            selectedCategory = getSelectedChipText(cgKategori)
            selectedPriority = getSelectedChipText(cgPrioritas)
            selectedStatus = getSelectedChipText(cgStatus)
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun populateChipGroup(chipGroup: ChipGroup, items: List<String>, selectedItem: String?) {
        chipGroup.removeAllViews()
        for (item in items) {
            val chip = Chip(this)
            chip.text = item
            chip.isCheckable = true
            chip.isChecked = (item == selectedItem)
            chipGroup.addView(chip)
        }
    }

    private fun getSelectedChipText(chipGroup: ChipGroup): String? {
        val checkedId = chipGroup.checkedChipId
        if (checkedId != View.NO_ID) {
            val chip = chipGroup.findViewById<Chip>(checkedId)
            return chip.text.toString()
        }
        return null
    }

    private fun animateFabPopIn() {
        binding.fabAdd.visibility = View.INVISIBLE
        binding.fabAdd.scaleX = 0f
        binding.fabAdd.scaleY = 0f
        binding.fabAdd.alpha = 0f
        
        binding.fabAdd.postDelayed({
            binding.fabAdd.visibility = View.VISIBLE
            binding.fabAdd.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .start()
        }, 300)
    }

    private fun showDeleteDialog(notes: Notes) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        txtMessage.text = "Apakah Anda yakin ingin menghapus catatan '${notes.judul}'?"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            val result = dbHelper.deleteData(notes.id)
            if (result > 0) {
                Toast.makeText(this, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                loadData()
            } else {
                Toast.makeText(this, "Data Gagal Dihapus", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        var listNotes = dbHelper.getAllData()
        
        if (searchQuery.isNotEmpty()) {
            listNotes = listNotes.filter { 
                it.judul.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedCategory != null) {
            listNotes = listNotes.filter { it.kategori == selectedCategory }
        }

        if (selectedPriority != null) {
            listNotes = listNotes.filter { it.prioritas == selectedPriority }
        }

        if (selectedStatus != null) {
            listNotes = listNotes.filter { it.status == selectedStatus }
        }

        Log.d("MainActivity", "Filtered data size: ${listNotes.size}")
        adapter.updateData(listNotes)
    }
}
