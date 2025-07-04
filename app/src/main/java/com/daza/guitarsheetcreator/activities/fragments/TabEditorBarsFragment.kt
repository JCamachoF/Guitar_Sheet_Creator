package com.daza.guitarsheetcreator.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.activities.TabEditorActivity
import com.daza.guitarsheetcreator.activities.adapters.TabBarAdapterEdit
import com.daza.guitarsheetcreator.activities.dataBase.AppDatabase
import com.daza.guitarsheetcreator.activities.models.TablatureBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [TabEditorBarsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabEditorBarsFragment : Fragment() {

    lateinit var adapter: TabBarAdapterEdit
    val bars = mutableListOf<TablatureBar>()
    private var songId: Int = -1

    companion object {
        fun newInstance(songId: Int) = TabEditorBarsFragment().apply {
            arguments = Bundle().apply {
                putInt("song_id", songId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songId = arguments?.getInt("song_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_editor_bars, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewBars)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa el adapter con una lista vacía
        adapter = TabBarAdapterEdit(
            bars,
            onStringClicked = { barIndex, string ->
                (activity as? TabEditorActivity)?.showFretNumbers(barIndex, string)
            },
            showPositionGuide = true // ✅ Se muestra la guía de posición
        )

        recyclerView.adapter = adapter

        // Vaciar lista primero
        bars.clear()

        if (songId != -1) {
            // Cargar desde base de datos
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val song = db.songDao().getSongById(songId)

                withContext(Dispatchers.Main) {
                    bars.clear()

                    if (song != null && song.tablature.isNotBlank()) {
                        val restoredBars = parseTablature(song.tablature)
                        bars.addAll(restoredBars)
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            // No es edición, sino creación: barra vacía
            bars.add(TablatureBar())
            adapter.notifyItemInserted(0)
        }


        return view
    }


    // Aquí el método para parsear la tablatura de String a lista de TablatureBar
    fun parseTablature(tablatureText: String): List<TablatureBar> {
        val bars = mutableListOf<TablatureBar>()
        val lines = tablatureText.lines().filter { it.isNotBlank() }

        for (i in lines.indices step 6) {
            if (i + 5 >= lines.size) break

            val bar = TablatureBar().apply {
                highE.clear()
                highE.addAll(lines[i].removePrefix("e|").chunked(3))
                B.clear()
                B.addAll(lines[i + 1].removePrefix("B|").chunked(3))
                G.clear()
                G.addAll(lines[i + 2].removePrefix("G|").chunked(3))
                D.clear()
                D.addAll(lines[i + 3].removePrefix("D|").chunked(3))
                A.clear()
                A.addAll(lines[i + 4].removePrefix("A|").chunked(3))
                lowE.clear()
                lowE.addAll(lines[i + 5].removePrefix("E|").chunked(3))
            }
            bars.add(bar)
        }
        return bars
    }

    // Método para construir el string de tablatura desde la lista de TablatureBar
    fun buildTabString(bars: List<TablatureBar>): String {
        return bars.joinToString("\n\n") { bar ->
            listOf(
                "e|${bar.highE.joinToString("")}",
                "B|${bar.B.joinToString("")}",
                "G|${bar.G.joinToString("")}",
                "D|${bar.D.joinToString("")}",
                "A|${bar.A.joinToString("")}",
                "E|${bar.lowE.joinToString("")}"
            ).joinToString("\n")
        }
    }

    fun updateFretAt(barIndex: Int, string: String, fret: String, position: Int) {
        val bar = bars[barIndex]
        val padded = fret.padStart(2, '-').padEnd(3, '-')

        fun ensureSize(list: MutableList<String>, pos: Int) {
            while (list.size <= pos) list.add("  -")
        }

        when (string) {
            "e" -> ensureSize(bar.highE, position).also { bar.highE[position] = padded }
            "B" -> ensureSize(bar.B, position).also { bar.B[position] = padded }
            "G" -> ensureSize(bar.G, position).also { bar.G[position] = padded }
            "D" -> ensureSize(bar.D, position).also { bar.D[position] = padded }
            "A" -> ensureSize(bar.A, position).also { bar.A[position] = padded }
            "E" -> ensureSize(bar.lowE, position).also { bar.lowE[position] = padded }
        }

        adapter.notifyItemChanged(barIndex)
    }

}