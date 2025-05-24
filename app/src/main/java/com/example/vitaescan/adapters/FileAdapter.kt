package com.example.vitaescan.adapters

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitaescan.R

class FileAdapter(
    private var archivos: List<Map<String, Any>>,
    private val context: Context,
    private val onAction: (Map<String, Any>, String) -> Unit
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
        val nombre = archivo["nombre"] as? String ?: "Desconocido"
        val documento = archivo["documento"] as? String ?: "Sin doc"
        holder.tvFileName.text = "$documento - $nombre"

        holder.ivOptions.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.ivOptions)
            popupMenu.menuInflater.inflate(R.menu.file_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_view -> onAction(archivo, "view")
                    R.id.action_delete -> onAction(archivo, "delete")
                    R.id.action_categorize -> onAction(archivo, "categorize")
                    else -> false
                }
                true
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = archivos.size

    fun updateList(nuevaLista: List<Map<String, Any>>) {
        archivos = nuevaLista
        notifyDataSetChanged()
    }
}
