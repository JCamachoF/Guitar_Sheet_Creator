package com.daza.guitarsheetcreator.activities.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daza.guitarsheetcreator.activities.models.Song

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(song: Song)


    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    suspend fun searchSongs(query: String): List<Song>


    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: Int): Song?

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()




}