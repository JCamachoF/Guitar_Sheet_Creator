
package com.daza.guitarsheetcreator.activities.activities

import android.content.Intent

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.daza.guitarsheetcreator.activities.dataBase.Backup
import com.google.android.material.navigation.NavigationView

lateinit var drawerLayout: DrawerLayout
lateinit var navigationView: NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var db: AppDatabase

    private lateinit var toolbar: Toolbar

    private val createBackupFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                lifecycleScope.launch {
                    val repository = Backup(this@MainActivity, db.songDao())
                    val songs = db.songDao().getAllSongs()
                    repository.exportBackup(it, songs)
                }
            }
        }

    private val pickBackupFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                lifecycleScope.launch {
                    val repository = Backup(this@MainActivity, db.songDao())
                    repository.restoreBackup(it)
                    loadSongs()
                }
            }
        }



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
        supportActionBar?.title = "Guitar Sheet"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_backup -> backupDatabase()
                R.id.nav_restore -> restoreDataFromCel()
            }
            drawerLayout.closeDrawers()
            true
        }


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
   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                backupDatabase()
                true
            }

            R.id.action_open -> {
                restoreDataFromCel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun restoreDataFromCel() {
        pickBackupFile.launch(arrayOf("application/json"))

    }

    private fun backupDatabase() {
        createBackupFile.launch("GuitarSheet Backup.json")
    }


    //funcion para cerrar el drawer al presionar botón hacia atras
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



}