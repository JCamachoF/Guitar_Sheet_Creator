package com.daza.guitarsheetcreator.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView


import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.activities.TabEditorActivity


class TabEditorControlsFragment : Fragment() {

    //para la flecha hacia atras
    sealed class EditorAction {

        data class FretAction(
            val barIndex: Int,
            val string: String,
            val fret: String,
            val position: Int
        ) : EditorAction()

        data class AddBarAction(
            val barIndex: Int
        ) : EditorAction()
    }
    private val actionHistory = mutableListOf<EditorAction>()


    // Callbacks para comunicar con la actividad
    private var onAddBar: (() -> Unit)? = null
    private var onSave: (() -> Unit)? = null
    private var onUndo: (() -> Unit)? = null

    private lateinit var containerDynamicButtons: LinearLayout

    private lateinit var gridTrastes: GridLayout



    fun setOnAddBarListener(listener: () -> Unit) {
        onAddBar = listener
    }

    fun setOnSaveListener(listener: () -> Unit) {
        onSave = listener
    }

    fun setOnUndoListener(listener: () -> Unit) {
        onUndo = listener

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_editor_controls, container, false)

        containerDynamicButtons = view.findViewById(R.id.container_dynamic_buttons)
        gridTrastes = view.findViewById(R.id.grid_trastes)

        view.findViewById<Button>(R.id.buttonAddBar).setOnClickListener {

            // Guardamos acción añadir tab
            val barIndex = (activity as TabEditorActivity).getBarsCount()

            actionHistory.add(
                EditorAction.AddBarAction(barIndex)
            )

            onAddBar?.invoke()
        }


        view.findViewById<Button>(R.id.buttonSaveTabEditor).setOnClickListener {
            onSave?.invoke()
        }

        view.findViewById<ImageButton>(R.id.buttonUndo).setOnClickListener {
            onUndo?.invoke()
        }


        return view
    }

    companion object {
        fun newInstance() = TabEditorControlsFragment()
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
            setPadding(16, 4, 16, 4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
                bottomMargin = 4
            }
        }

        containerDynamicButtons.addView(instruccionTextView)
        containerDynamicButtons.addView(gridTrastes)

        val trastes = listOf("X", "---", "|") + (0..15).map { it.toString() }

        gridTrastes.columnCount = 5
        gridTrastes.removeAllViews()

        val scale = resources.displayMetrics.density
        val buttonHeight = (40 * scale).toInt() // altura de 40dp

        for (traste in trastes) {
            val trasteButton = Button(requireContext()).apply {
                text = traste
                isAllCaps = false

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = buttonHeight
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(2, 1, 2, 1)
                }

                setPadding(4, 2, 4, 2)

                setOnClickListener {
                    currentFret = traste
                    if (traste == "|") {
                        showGlobalPositionSelection(instruccionTextView)
                    } else {
                        showPositionSelection(instruccionTextView, traste)
                    }
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
                topMargin = 5
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
                    // Guardamos acción
                    actionHistory.add(
                        EditorAction.FretAction(
                            barIndex = currentBarIndex,
                            string = currentString,
                            fret = fret,
                            position = pos - 1
                        )
                    )

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

    fun undoLastAction(): EditorAction? {
        if (actionHistory.isEmpty()) return null
        return actionHistory.removeAt(actionHistory.lastIndex)
    }

    private fun showGlobalPositionSelection(textView: TextView) {
        textView.text = "Selecciona la posición del compás para el separador |"
        gridTrastes.removeAllViews()

        val posiciones = (1..10)

        for (pos in posiciones) {
            val posButton = Button(requireContext()).apply {
                text = pos.toString()
                isAllCaps = false
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    // Guardamos acción (opcional para undo)
                    actionHistory.add(
                        EditorAction.FretAction(
                            barIndex = currentBarIndex,
                            string = "ALL",
                            fret = "|",
                            position = pos - 1
                        )
                    )

                    currentFretSelectionCallback?.invoke("|", pos - 1)
                    clearFretSelectionUI()
                }
            }
            gridTrastes.addView(posButton)
        }
    }
}