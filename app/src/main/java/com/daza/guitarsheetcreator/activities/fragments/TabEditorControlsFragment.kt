package com.daza.guitarsheetcreator.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.daza.guitarsheetcreator.R



class TabEditorControlsFragment : Fragment() {


    // Callbacks para comunicar con la actividad
    private var onAddBar: (() -> Unit)? = null
    private var onSave: (() -> Unit)? = null

    private lateinit var containerDynamicButtons: LinearLayout

    private lateinit var gridTrastes: GridLayout

    private var currentOnFretSelected: ((String) -> Unit)? = null


    fun setOnAddBarListener(listener: () -> Unit) {
        onAddBar = listener
    }

    fun setOnSaveListener(listener: () -> Unit) {
        onSave = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_editor_controls, container, false)

        containerDynamicButtons = view.findViewById(R.id.container_dynamic_buttons)
        gridTrastes = view.findViewById(R.id.grid_trastes)

        view.findViewById<Button>(R.id.buttonAddBar).setOnClickListener {
            onAddBar?.invoke()
        }

        view.findViewById<Button>(R.id.buttonSaveTabEditor).setOnClickListener {
            onSave?.invoke()
        }



        return view
    }

    companion object {
        fun newInstance() = TabEditorControlsFragment()
    }

    private fun addButton(cuerda: String) {
        // Limpiar contenedor
        containerDynamicButtons.removeAllViews()
        gridTrastes.removeAllViews()

        // Crear TextView de instrucción (en lugar de botón)
        val instruccionTextView = TextView(requireContext()).apply {
            text = "Selecciona el traste para la cuerda $cuerda"
            textSize = 16f
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
        }

        // Agregamos la instrucción arriba
        containerDynamicButtons.addView(instruccionTextView)

        // Agregamos el GridLayout debajo
        containerDynamicButtons.addView(gridTrastes)

        // Trastes del 1 al 10 + "x"
        val trastes = listOf("x" , "...") + (1..10).map { it.toString() }

        for (traste in trastes) {
            val trasteButton = Button(requireContext()).apply {
                text = traste
                isAllCaps = false
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    Toast.makeText(context, "Cuerda $cuerda - Traste $traste", Toast.LENGTH_SHORT).show()
                }
            }

            gridTrastes.addView(trasteButton)
        }
    }


    private var currentFretSelectionCallback: ((String, Int) -> Unit)? = null
    private var currentString: String = ""
    private var currentBarIndex: Int = -1
    private var currentFret: String? = null

    fun startFretSelection(barIndex: Int, string: String, onComplete: (String, Int) -> Unit) {
        currentBarIndex = barIndex
        currentString = string
        currentFret = null
        currentFretSelectionCallback = onComplete

        containerDynamicButtons.removeAllViews()
        gridTrastes.removeAllViews()

        val instruccionTextView = TextView(requireContext()).apply {
            text = "Selecciona el traste para la cuerda $string"
            textSize = 16f
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
        }

        containerDynamicButtons.addView(instruccionTextView)
        containerDynamicButtons.addView(gridTrastes)

        val trastes = listOf("X", "---") + (0..12).map { it.toString() }

        gridTrastes.columnCount = 5
        gridTrastes.removeAllViews()

        for (traste in trastes) {
            val trasteButton = Button(requireContext()).apply {
                text = traste
                isAllCaps = false
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    currentFret = traste
                    showPositionSelection(instruccionTextView, traste)
                }
            }
            gridTrastes.addView(trasteButton)
        }

        // 🔹 Botón Cancelar
        val cancelButton = Button(requireContext()).apply {
            text = "Cancelar"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
            setOnClickListener {
                clearFretSelectionUI()
            }
        }

        containerDynamicButtons.addView(cancelButton)
    }

    private fun showPositionSelection(textView: TextView, fret: String) {
        textView.text = "Selecciona la posición del compás para la cuerda $currentString (traste $fret)"
        gridTrastes.removeAllViews()

        val posiciones = (1..10).toList()

        for (pos in posiciones) {
            val posButton = Button(requireContext()).apply {
                text = pos.toString()
                isAllCaps = false
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    currentFretSelectionCallback?.invoke(fret, pos - 1)
                    clearFretSelectionUI()
                }
            }
            gridTrastes.addView(posButton)
        }
    }

    private fun clearFretSelectionUI() {
        containerDynamicButtons.removeAllViews()
        gridTrastes.removeAllViews()
        currentFretSelectionCallback = null
        currentString = ""
        currentFret = null
        currentBarIndex = -1
    }


}