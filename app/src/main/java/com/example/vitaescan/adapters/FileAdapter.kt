package com.example.vitaescan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitaescan.R

data class FileItem(val documento: String, val nombre: String)

class FileAdapter(
    private var archivos: List<FileItem>,
    private val onAction: (FileItem, String) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val ivOptions: ImageView = view.findViewById(R.id.ivOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val archivo = archivos[position]
        holder.tvFileName.text = archivo.nombre

        holder.ivOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.ivOptions)
            popupMenu.menuInflater.inflate(R.menu.file_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_view -> onAction(archivo, "view") // Llama a la acción "view"
                    R.id.action_delete -> onAction(archivo, "delete")
                    R.id.action_categorize -> onAction(archivo, "categorize")
                }
                true
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = archivos.size

    // Método para actualizar la lista
    fun updateList(newList: List<FileItem>) {
        archivos = newList
        notifyDataSetChanged()
    }
}