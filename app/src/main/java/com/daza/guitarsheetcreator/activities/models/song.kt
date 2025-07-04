package com.daza.guitarsheetcreator.activities.models


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val notes: String,
    val tablature: String,
    val tags: String = "",
    val isFavorite: Boolean = false
)
