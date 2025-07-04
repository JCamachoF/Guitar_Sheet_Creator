package com.daza.guitarsheetcreator.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.models.TablatureBar

class TablatureRowAdapter(
    private val groupedBars: List<List<TablatureBar>>
) : RecyclerView.Adapter<TablatureRowAdapter.RowViewHolder>() {

    inner class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eLine: TextView = itemView.findViewById(R.id.textViewe)
        val bLine: TextView = itemView.findViewById(R.id.textViewB)
        val gLine: TextView = itemView.findViewById(R.id.textViewG)
        val dLine: TextView = itemView.findViewById(R.id.textViewD)
        val aLine: TextView = itemView.findViewById(R.id.textViewA)
        val eLowLine: TextView = itemView.findViewById(R.id.textViewE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bar, parent, false)
        return RowViewHolder(view)
    }

    override fun getItemCount(): Int = groupedBars.size

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val barsInRow = groupedBars[position]

        holder.eLine.text = "e|" + barsInRow.joinToString("") { it.highE.joinToString("") }
        holder.bLine.text = "B|" + barsInRow.joinToString("") { it.B.joinToString("") }
        holder.gLine.text = "G|" + barsInRow.joinToString("") { it.G.joinToString("") }
        holder.dLine.text = "D|" + barsInRow.joinToString("") { it.D.joinToString("") }
        holder.aLine.text = "A|" + barsInRow.joinToString("") { it.A.joinToString("") }
        holder.eLowLine.text = "E|" + barsInRow.joinToString("") { it.lowE.joinToString("") }
    }
}