package com.zaky.ruangku

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (context: Context) : SQLiteOpenHelper(context, "NotesDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query_table =
            "CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, judul TEXT, catatan TEXT, status TEXT, tanggal TEXT, kategori TEXT, prioritas TEXT)"
        db?.execSQL(query_table)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Metode ini akan menghapus tabel lama dan membuat ulang saat versi database dinaikkan
        db?.execSQL("DROP TABLE IF EXISTS notes")
        onCreate(db)
    }

    fun insertData(judul: String, catatan: String, status: String, tanggal: String, kategori: String, prioritas: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("judul", judul)
        values.put("catatan", catatan)
        values.put("status", status)
        values.put("tanggal", tanggal)
        values.put("kategori", kategori)
        values.put("prioritas", prioritas)
        return db.insert("notes", null, values)
    }

    fun updateData(id: Int, judul: String, catatan: String, status: String, tanggal: String, kategori: String, prioritas: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("judul", judul)
        values.put("catatan", catatan)
        values.put("status", status)
        values.put("tanggal", tanggal)
        values.put("kategori", kategori)
        values.put("prioritas", prioritas)
        return db.update("notes", values, "id = ?", arrayOf(id.toString()))
    }

    fun getAllData(): List<Notes> {
        val list = ArrayList<Notes>()
        val db = this.readableDatabase
        val query = "SELECT * FROM notes"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Notes(
                    cursor.getInt(0),
                    cursor.getString(1) ?: "",
                    cursor.getString(2) ?: "",
                    cursor.getString(3) ?: "",
                    cursor.getString(4) ?: "",
                    cursor.getString(5) ?: "",
                    cursor.getString(6) ?: "")
                )
                } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteData(id: Int): Int {
        val db = this.writableDatabase
        return db.delete("notes", "id = ?", arrayOf(id.toString()))
    }
}
