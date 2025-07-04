package com.daza.guitarsheetcreator.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.models.TablatureBar

class TabBarAdapterEdit(
    private val bars: List<TablatureBar>,
    private val onStringClicked: (barIndex: Int, stringName: String) -> Unit,
    private val showPositionGuide: Boolean = true // nuevo parámetro opcional
) : RecyclerView.Adapter<TabBarAdapterEdit.TabBarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabBarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bar, parent, false)
        return TabBarViewHolder(view)
    }

    override fun getItemCount() = bars.size

    override fun onBindViewHolder(holder: TabBarViewHolder, position: Int) {
        holder.bind(bars[position], position)
        val positionGuide = (1..10).joinToString("  ") { it.toString().padStart(2, ' ') }


    }

    inner class TabBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(bar: TablatureBar, barIndex: Int) {

            val strings = mapOf(
                "e" to itemView.findViewById<TextView>(R.id.textViewe),
                "B" to itemView.findViewById<TextView>(R.id.textViewB),
                "G" to itemView.findViewById<TextView>(R.id.textViewG),
                "D" to itemView.findViewById<TextView>(R.id.textViewD),
                "A" to itemView.findViewById<TextView>(R.id.textViewA),
                "E" to itemView.findViewById<TextView>(R.id.textViewE),
                "p" to itemView.findViewById<TextView>(R.id.compas_position)

            )

            val positionTextView = strings["p"]
            if (!showPositionGuide) {
                positionTextView?.visibility = View.GONE
            }




            for ((name, view) in strings) {
                val prefix = when (name) {
                    "e" -> "e|"
                    "B" -> "B|"
                    "G" -> "G|"
                    "D" -> "D|"
                    "A" -> "A|"
                    "E" -> "E|"
                    "p" -> " |"
                    else -> ""
                }

                val content = when (name) {
                    "e" -> bar.highE.joinToString("")  // ejemplo: "  -  -  3-  -"
                    "B" -> bar.B.joinToString("")
                    "G" -> bar.G.joinToString("")
                    "D" -> bar.D.joinToString("")
                    "A" -> bar.A.joinToString("")
                    "E" -> bar.lowE.joinToString("")
                    "p" -> {
                        val fixedCount = 10
                        val numbers = (1..fixedCount).map {
                            it.toString().padStart(2, ' ').padEnd(3, ' ')
                        }.joinToString("")

                        val guide = numbers.map { ch ->
                            if (ch == ' ') '-' else ch
                        }.joinToString("")

                        guide
                    }


                    else -> ""
                }

                view.text = prefix + content

                if (name != "p") {
                    view.setOnClickListener {
                        onStringClicked(barIndex, name)
                    }
                }

            }
        }


    }
}