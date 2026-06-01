package com.daza.guitarsheetcreator.activities.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.models.TablatureBar

class TabBarEditorAdapter(
    private val bars: List<TablatureBar>,
    private val onCellClicked: (barIndex: Int, stringName: String, position: Int) -> Unit
) : RecyclerView.Adapter<TabBarEditorAdapter.TabBarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabBarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bar, parent, false)
        return TabBarViewHolder(view)
    }

    override fun getItemCount() = bars.size

    override fun onBindViewHolder(holder: TabBarViewHolder, position: Int) {
        holder.bind(bars[position], position)
    }

    inner class TabBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // LinearLayouts para cada cuerda
        private val stringRows: Map<String, LinearLayout> = mapOf(
            "e" to itemView.findViewById(R.id.row_e),
            "B" to itemView.findViewById(R.id.row_B),
            "G" to itemView.findViewById(R.id.row_G),
            "D" to itemView.findViewById(R.id.row_D),
            "A" to itemView.findViewById(R.id.row_A),
            "E" to itemView.findViewById(R.id.row_E)
        )

        fun bind(bar: TablatureBar, barIndex: Int) {

            for ((stringName, container) in stringRows) {
                container.removeAllViews()

                // Usamos directamente las propiedades de TablatureBar
                val notes = when(stringName) {
                    "e" -> bar.highE
                    "B" -> bar.B
                    "G" -> bar.G
                    "D" -> bar.D
                    "A" -> bar.A
                    "E" -> bar.lowE
                    else -> mutableListOf()
                }

                for ((pos, note) in notes.withIndex()) {
                    val cell = TextView(container.context).apply {
                        text = note
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        includeFontPadding = false
                        gravity = Gravity.CENTER
                        setPadding(0,0,0,0)
                    }
                    cell.setTextColor(Color.BLACK)
                   // cell.setBackgroundResource(R.drawable.cell_background) // opcional

                    val params = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                    params.setMargins(0, 0, 0, 0)
                    cell.layoutParams = params
                    cell.textSize = 13f

                    // Click en cada celda
                    cell.setOnClickListener {
                        onCellClicked(barIndex, stringName, pos)
                    }

                    container.addView(cell)
                }
            }
        }
    }
}
