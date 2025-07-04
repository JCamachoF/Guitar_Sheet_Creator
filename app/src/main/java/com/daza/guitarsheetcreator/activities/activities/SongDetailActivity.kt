package com.daza.guitarsheetcreator.activities.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.daza.guitarsheetcreator.activities.dataBase.AppDatabase
import com.daza.guitarsheetcreator.R
import com.daza.guitarsheetcreator.activities.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongDetailActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var artistEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var buttonTablatureEdit: Button

    private var songId: Int = 0
    private lateinit var currentSong: Song
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_song_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbarDetail)
        // Configura la toolbar como ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Guitar Sheet Creator"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        titleEditText = findViewById(R.id.editTextTitle)
        artistEditText = findViewById(R.id.editTextArtist)
        tagsEditText = findViewById(R.id.editTextTags)
        notesEditText = findViewById(R.id.editTextNotes)
        saveButton = findViewById(R.id.buttonSave)
        deleteButton = findViewById(R.id.buttonDelete)
        buttonTablatureEdit = findViewById(R.id.buttonTablatureEdit)

        songId = intent.getIntExtra("SONG_ID", 0)

        if (songId == 0) {
            Toast.makeText(this, "Error: canción no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadSong()

        saveButton.setOnClickListener {
            saveChanges()
        }

        deleteButton.setOnClickListener {
            confirmDelete()
        }

        buttonTablatureEdit.setOnClickListener {

            val intent = Intent(this, TabEditorActivity::class.java)
            intent.putExtra("song_id", songId)
            startActivity(intent)
            finish()


        }
    }

    private fun loadSong() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.Companion.getDatabase(this@SongDetailActivity)
            val song = db.songDao().getSongById(songId)
            if (song == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SongDetailActivity,
                        "Canción no encontrada",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } else {
                currentSong = song
                withContext(Dispatchers.Main) {
                    titleEditText.setText(song.title)
                    artistEditText.setText(song.artist)
                    tagsEditText.setText(song.tags)
                    notesEditText.setText(song.notes)
                }
            }
        }
    }

    private fun saveChanges() {
        val newTitle = titleEditText.text.toString()
        val newArtist = artistEditText.text.toString()
        val newTags = tagsEditText.text.toString()
        val newNotes = notesEditText.text.toString()

        if (newTitle.isBlank()) {
            Toast.makeText(this, "Título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val updatedSong = currentSong.copy(
                title = newTitle,
                artist = newArtist,
                tags = newTags,
                notes = newNotes
            )
            AppDatabase.Companion.getDatabase(this@SongDetailActivity).songDao().update(updatedSong)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SongDetailActivity, "Cambios guardados", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar canción")
            .setMessage("¿Seguro que quieres eliminar esta canción?")
            .setPositiveButton("Sí") { _, _ -> deleteSong() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteSong() {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.Companion.getDatabase(this@SongDetailActivity).songDao().delete(currentSong)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SongDetailActivity, "Canción eliminada", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }


}