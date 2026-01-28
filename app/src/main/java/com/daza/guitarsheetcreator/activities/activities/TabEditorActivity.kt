package com.daza.guitarsheetcreator.activities.activities


import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.dataBase.AppDatabase
import com.daza.guitarsheetcreator.activities.fragments.TabEditorBarsFragment
import com.daza.guitarsheetcreator.activities.fragments.TabEditorControlsFragment
import com.daza.guitarsheetcreator.activities.models.Song
import com.daza.guitarsheetcreator.activities.models.TablatureBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TabEditorActivity : AppCompatActivity() {
    private var songId: Int = -1

    // Fragmentos como propiedades para acceso en toda la actividad
    private lateinit var barsFragment: TabEditorBarsFragment
    private lateinit var controlsFragment: TabEditorControlsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tab_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songId = savedInstanceState?.getInt("song_id") ?: intent.getIntExtra("song_id", -1)

        if (savedInstanceState == null) {

            barsFragment = TabEditorBarsFragment.newInstance(songId)
            controlsFragment = TabEditorControlsFragment.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.controlsFragment, controlsFragment, "controlsFragment")
                .replace(R.id.fragmentBarsContainer, barsFragment, "barsFragment")
                .commit()
        } else {
            barsFragment = supportFragmentManager.findFragmentByTag("barsFragment") as TabEditorBarsFragment
            controlsFragment = supportFragmentManager.findFragmentByTag("controlsFragment") as TabEditorControlsFragment
        }

        // 🔗 conecta listeners sin duplicar fragmentos
        controlsFragment.setOnAddBarListener {
            barsFragment.bars.add(TablatureBar())
            barsFragment.adapter.notifyItemInserted(barsFragment.bars.size - 1)
        }

        controlsFragment.setOnSaveListener {
            guardarTablatura()
        }

        controlsFragment.setOnUndoListener {
            deshacerUltimaAccion()
        }
    }

    private fun guardarTablatura() {
        val bars = barsFragment.bars
        val tablatureText = barsFragment.buildTabString(bars)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@TabEditorActivity)

            if (songId == -1) {
                // Aquí debes obtener los datos para la nueva canción (puedes pasar por intent o pedir al usuario)
                val title = intent.getStringExtra("title") ?: "Sin título"
                val artist = intent.getStringExtra("artist") ?: "Desconocido"
                val notes = intent.getStringExtra("notes") ?: ""
                val tags = intent.getStringExtra("tags") ?: ""

                val newSong = Song(
                    title = title,
                    artist = artist,
                    notes = notes,
                    tablature = tablatureText,
                    tags = tags
                )
                db.songDao().insert(newSong)
            } else {
                val existingSong = db.songDao().getSongById(songId)
                if (existingSong != null) {
                    val updated = existingSong.copy(tablature = tablatureText)
                    db.songDao().update(updated)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@TabEditorActivity, "Tablatura guardada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun showFretNumbers(barIndex: Int, string: String) {

        controlsFragment.startFretSelection(barIndex, string) { selectedFret, selectedPosition ->

            if (selectedFret == "|") {
                barsFragment.updateBarSeparatorAt(barIndex, selectedPosition)
            } else {
                barsFragment.updateFretAt(
                    barIndex,
                    string,
                    selectedFret,
                    selectedPosition
                )
            }
        }
    }

    // Este método se llama automáticamente antes de que Android
    //  destruya tu actividad (como al girar la pantalla).
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("song_id", songId)
    }

    private fun deshacerUltimaAccion() {
        val action = controlsFragment.undoLastAction()

        if (action == null) {
            Toast.makeText(
                this,
                "No hay cambios para deshacer",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when (action) {

            is TabEditorControlsFragment.EditorAction.FretAction -> {
                if (action.fret == "|" && action.string == "ALL") {
                    barsFragment.undoBarSeparator(
                        barIndex = action.barIndex,
                        position = action.position)
                } else {
                    barsFragment.clearFretAt(
                        barIndex = action.barIndex,
                        string = action.string,
                        position = action.position
                    )
                }
            }

            is TabEditorControlsFragment.EditorAction.AddBarAction -> {
                barsFragment.removeLastBar()
            }
        }
    }
    fun getBarsCount(): Int {
        return barsFragment.bars.size
    }
}