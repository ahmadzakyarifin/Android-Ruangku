package com.zaky.ruangku

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.zaky.ruangku.databinding.ActivityAddEditBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class AddEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DatabaseHelper
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Penanganan Insets agar toolbar mepet ke atas
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        dbHelper = DatabaseHelper(this)

        // Logic Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup Dropdowns
        binding.spinStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, Notes.PILIHAN_STATUS))
        binding.spinKategori.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, Notes.PILIHAN_KATEGORI))
        binding.spinPrioritas.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, Notes.PILIHAN_PRIORITAS))

        binding.txtTanggal.setOnClickListener {
            showMaterialDatePicker()
        }

        val note = intent.getSerializableExtra("EXTRA_NOTE") as? Notes
        if (note != null) {
            noteId = note.id
            binding.edtJudul.setText(note.judul)
            binding.edtCatatan.setText(note.catatan)
            binding.txtTanggal.setText(note.tanggal)

            binding.spinStatus.setText(note.status, false)
            binding.spinKategori.setText(note.kategori, false)
            binding.spinPrioritas.setText(note.prioritas, false)

            binding.txtHeader.text = "Edit Catatan"
            binding.btnSubmit.text = "PERBARUI"
        } else {
            binding.txtHeader.text = "Tambah Catatan"
            binding.btnSubmit.text = "SIMPAN"
        }

        binding.btnSubmit.setOnClickListener {
            simpanData()
        }
    }


    private fun showMaterialDatePicker() {
        // Menggunakan tema kustom agar kalender selalu Putih/Biru
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setTheme(R.style.CustomMaterialCalendar)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = (selection as Long)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.txtTanggal.setText(format.format(calendar.time))
        }

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun simpanData() {
        val judul = binding.edtJudul.text.toString()
        val catatan = binding.edtCatatan.text.toString()
        val status = binding.spinStatus.text.toString()
        val kategori = binding.spinKategori.text.toString()
        val prioritas = binding.spinPrioritas.text.toString()
        val tanggal = binding.txtTanggal.text.toString()

        if (judul.isEmpty() || catatan.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val result = if (noteId == -1) {
            dbHelper.insertData(judul, catatan, status, tanggal, kategori, prioritas)
        } else {
            dbHelper.updateData(noteId, judul, catatan, status, tanggal, kategori, prioritas).toLong()
        }

        if (result != -1L) {
            Toast.makeText(this, if (noteId == -1) "Catatan berhasil disimpan" else "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
