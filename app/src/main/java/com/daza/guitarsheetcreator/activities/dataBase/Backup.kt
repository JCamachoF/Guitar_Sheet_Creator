package com.daza.guitarsheetcreator.activities.dataBase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.daza.guitarsheetcreator.activities.models.Song
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Backup(
    private val context: Context,
    private val songDao: SongDao
) {

    private val gson = Gson()

    /**
     * =========================
     * EXPORTAR BACKUP
     * =========================
     * Se debe pasar el Uri obtenido desde SAF (CreateDocument)
     */
    suspend fun exportBackup(uri: Uri, songs: List<Song>) {
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(songs)

                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray())
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Backup exportado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Backup", "Error exportando: ${e.message}", e)
            }
        }
    }

    /**
     * =========================
     * RESTAURAR BACKUP
     * =========================
     * Se debe pasar el Uri obtenido desde SAF (OpenDocument)
     */
    suspend fun restoreBackup(uri: Uri, replaceDatabase: Boolean = true) {
        withContext(Dispatchers.IO) {
            try {

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val json = input.bufferedReader().use { it.readText() }
                    val songs = gson.fromJson(json, Array<Song>::class.java)

                    if (replaceDatabase) {
                        songDao.deleteAllSongs() // Debes tener este método en tu DAO
                    }

                    songs.forEach { songDao.insert(it) }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Backup restaurado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Backup", "Error restaurando: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al restaurar el archivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}