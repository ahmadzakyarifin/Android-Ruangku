package com.zaky.ruangku

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zaky.ruangku.databinding.ItemNoteBinding
import android.view.LayoutInflater
import android.view.animation.AnimationUtils

class NotesAdapter (
    private var noteList: List<Notes>,
    private val onClick: (Notes) -> Unit,
    private val onDelete: (Notes) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NotesViewsHolder>() {
    
    fun updateData(newList: List<Notes>) {
        this.noteList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotesViewsHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotesViewsHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NotesViewsHolder,
        position: Int
    ) {
        val dataNote = noteList[position]
        
        // Animasi fade-in slide-up untuk setiap item
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.slide_in_left)
        holder.itemView.startAnimation(animation)

        with(holder.binding){
            txtJudul.text = dataNote.judul
            txtCatatan.text = dataNote.catatan
            txtKategori.text = dataNote.kategori
            txtPrioritas.text = dataNote.prioritas
            txtTanggal.text = dataNote.tanggal
            
            root.setOnClickListener {
                onClick(dataNote)
            }
            
            btnDelete.setOnClickListener {
                onDelete(dataNote)
            }
        }
    }

    override fun getItemCount(): Int = noteList.size

    class NotesViewsHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)
}
