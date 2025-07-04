package com.daza.guitarsheetcreator.activities.activities

import android.content.Intent

import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.daza.guitarsheetcreator.activities.dataBase.AppDatabase
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.adapters.SongAdapter
import com.daza.guitarsheetcreator.activities.models.Song

import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java
import androidx.appcompat.widget.Toolbar




class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var db: AppDatabase

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        toolbar = findViewById(R.id.toolbar)
        // Configura la toolbar como ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Guitar Sheet Creator"
        supportActionBar?.setDisplayShowTitleEnabled(true)




        recyclerView = findViewById(R.id.recyclerViewSongs)
        adapter = SongAdapter(
            emptyList(),
            onItemClick = { song ->
                // Acción con clic normal
            },
            onLongClick = { } // no hace nada aún
        )



        recyclerView.adapter = adapter
        db = AppDatabase.Companion.getDatabase(this)
        adapter = SongAdapter(
            emptyList(),
            onItemClick = { song ->
                val intent = Intent(this@MainActivity, TablatureViewerActivity::class.java)
                intent.putExtra("SONG_ID", song.id)
                startActivity(intent)
            },
            onLongClick = { song ->
                showSongOptionsDialog(song)
            }
        )
        recyclerView.adapter = adapter
        loadSongs()

        loadSongs()

        val fabAddSong = findViewById<FloatingActionButton>(R.id.fabAddSong)
        fabAddSong.setOnClickListener {
            val intent = Intent(this, AddSongActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val songsList = db.songDao().getAllSongs()
            withContext(Dispatchers.Main) {
                adapter.updateSongs(songsList)
            }
        }
    }



    private fun showSongOptionsDialog(song: Song) {
        val options = arrayOf("Editar", "Cancelar")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Opciones para: ${song.title}")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Editar
                    val intent = Intent(this@MainActivity, SongDetailActivity::class.java)
                    intent.putExtra("SONG_ID", song.id)
                    startActivity(intent)
                }
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        loadSongs()
    }

}