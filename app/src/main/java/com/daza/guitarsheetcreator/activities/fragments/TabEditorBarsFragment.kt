package com.daza.guitarsheetcreator.activities.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.activities.TabEditorActivity
import com.daza.guitarsheetcreator.activities.adapters.TabBarEditorAdapter
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

    //Contiene lista bars
    //
    //Tiene el adapter
    //
    //Convierte entre texto y modelo
    //
    //Es el que realmente dibuja la tablatura.

    lateinit var adapter: TabBarEditorAdapter
    val bars = mutableListOf<TablatureBar>()
    //Cada TablatureBar tiene 6 listas:
    //highE
    //B
    //G
    //D
    //A
    //lowE

    /*Cada elemento de esas listas representa un traste formateado a 3 caracteres:

    Ejemplos:
    "---"
    "-5-"
    "-|-“*/
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

        //aca pasamos el numero de fila (cuerda) y columna (posición)
        adapter = TabBarEditorAdapter(bars, onCellClicked = { barIndex, stringName, position ->
            (activity as? TabEditorActivity)?.showFretNumbers(barIndex, stringName, position)
            //Cuando el usuario toca una cuerda se llama a controlsFragment.startFretSelection(...)
            // en Tab Editor Activity
        }   )


        recyclerView.adapter = adapter


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
        //Divide en líneas:
        //
        //val lines = tablatureText.lines()
        //
        //Y cada 6 líneas forma un compás:
        //
        //for (i in lines.indices step 6)
        val bars = mutableListOf<TablatureBar>()
        val lines = tablatureText.lines().filter { it.isNotBlank() }

        for (i in lines.indices step 6) {
            if (i + 5 >= lines.size) break

            val bar = TablatureBar().apply {
                //Divide cada línea en bloques de 3 caracteres. chunked(3)
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

    // Método para convertir a string desde la lista de TablatureBar
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
        //Asegura que la lista tenga tamaño suficiente
        //Inserta traste formateado:
        //barIndex → el índice del compás que queremos modificar en la lista bars.
        //
        //string → la cuerda que queremos actualizar (e, B, G, D, A, E).
        //
        //fret → el número de traste que vamos a colocar en esa cuerda (por ejemplo "3").
        //
        //position → la posición dentro de la cuerda donde se colocará el traste
        // (para mantener alineadas las notas en la tablatura).



        //bars es la lista de compases.
        //
        //Cada bar tiene listas para cada cuerda: highE, B, G, D, A, lowE.
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

    fun clearFretAt(barIndex: Int, string: String, position: Int) {
        val bar = bars[barIndex]

        when (string) {
            "e" -> bar.highE[position] = "---"
            "A" -> bar.A[position] = "---"
            "D" -> bar.D[position] = "---"
            "G" -> bar.G[position] = "---"
            "B" -> bar.B[position] = "---"
            "E" -> bar.lowE[position] = "---"
        }

        adapter.notifyItemChanged(barIndex)
    }

    fun updateBarSeparatorAt(barIndex: Int, position: Int) {
        val bar = bars[barIndex]

        // Asegura que todas las cuerdas tengan tamaño suficiente
        fun ensureSize(list: MutableList<String>, pos: Int) {
            while (list.size <= pos) {
                list.add("---")
            }
        }

        val separator = "-|-"   // mismo ancho que un traste (3 chars)

        ensureSize(bar.highE, position)
        ensureSize(bar.B, position)
        ensureSize(bar.G, position)
        ensureSize(bar.D, position)
        ensureSize(bar.A, position)
        ensureSize(bar.lowE, position)

        bar.highE[position] = separator
        bar.B[position] = separator
        bar.G[position] = separator
        bar.D[position] = separator
        bar.A[position] = separator
        bar.lowE[position] = separator

        adapter.notifyItemChanged(barIndex)
    }


    fun undoBarSeparator(barIndex: Int, position: Int) {
        val bar = bars.getOrNull(barIndex) ?: return

        fun clearAt(list: MutableList<String>) {
            if (position in list.indices) {
                list[position] = "---"
            }
        }

        clearAt(bar.highE)
        clearAt(bar.B)
        clearAt(bar.G)
        clearAt(bar.D)
        clearAt(bar.A)
        clearAt(bar.lowE)

        adapter.notifyItemChanged(barIndex)
    }


    fun removeLastBar() {
        if (bars.isEmpty()) return

        val lastIndex = bars.lastIndex
        bars.removeAt(lastIndex)
        adapter.notifyItemRemoved(lastIndex)
    }



}