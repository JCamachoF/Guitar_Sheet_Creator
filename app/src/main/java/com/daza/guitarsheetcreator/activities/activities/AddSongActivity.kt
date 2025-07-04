package com.daza.guitarsheetcreator.activities.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.daza.guitarsheetcreator.R

class AddSongActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var artistEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var buttonTabalatureEditor: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_song)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbarAdd)
        // Configura la toolbar como ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Guitar Sheet Creator"
        supportActionBar?.setDisplayShowTitleEnabled(true)


        titleEditText = findViewById(R.id.editTextTitle)
        artistEditText = findViewById(R.id.editTextArtist)
        tagsEditText = findViewById(R.id.editTextTags)
        notesEditText = findViewById(R.id.editTextNotes)
        buttonTabalatureEditor = findViewById(R.id.buttonTablature)

        buttonTabalatureEditor.setOnClickListener {
            goToTabEditor()

        }

    }

    private fun goToTabEditor() {

        val title = titleEditText.text.toString()
        val artist = artistEditText.text.toString()
        val tags = tagsEditText.text.toString()
        val notes = notesEditText.text.toString()


        if (title.isBlank()) {
            Toast.makeText(this, "El título y la tablatura son obligatorios", Toast.LENGTH_SHORT).show()
            return
        } else {
            val intent = Intent(this@AddSongActivity, TabEditorActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("artist", artist)
            intent.putExtra("tags", tags)
            intent.putExtra("notes", notes)
            startActivity(intent)
            finish()

        }





    }


}