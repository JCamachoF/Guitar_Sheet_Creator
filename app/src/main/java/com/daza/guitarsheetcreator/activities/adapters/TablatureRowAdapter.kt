package com.daza.guitarsheetcreator.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.models.TablatureBar

class TablatureRowAdapter(
    private val groupedBars: List<List<TablatureBar>>
) : RecyclerView.Adapter<TablatureRowAdapter.RowViewHolder>() {

    inner class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // LinearLayouts para cada cuerda
        val stringRows: Map<String, LinearLayout> = mapOf(
            "e" to itemView.findViewById(R.id.row_e),
            "B" to itemView.findViewById(R.id.row_B),
            "G" to itemView.findViewById(R.id.row_G),
            "D" to itemView.findViewById(R.id.row_D),
            "A" to itemView.findViewById(R.id.row_A),
            "E" to itemView.findViewById(R.id.row_E)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bar, parent, false)
        return RowViewHolder(view)
    }

    override fun getItemCount(): Int = groupedBars.size

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {

        val barsInRow = groupedBars[position]

        for ((stringName, container) in holder.stringRows) {

            container.removeAllViews()

            for (bar in barsInRow) {

                val notes = when(stringName) {
                    "e" -> bar.highE
                    "B" -> bar.B
                    "G" -> bar.G
                    "D" -> bar.D
                    "A" -> bar.A
                    "E" -> bar.lowE
                    else -> mutableListOf()
                }

                for (note in notes) {

                    val cell = TextView(container.context).apply {
                        text = note
                        textSize = 10f
                        typeface = android.graphics.Typeface.MONOSPACE
                        gravity = android.view.Gravity.CENTER
                        includeFontPadding = false
                    }

                    val params = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )

                    cell.layoutParams = params

                    container.addView(cell)
                }

            }
        }
    }
}