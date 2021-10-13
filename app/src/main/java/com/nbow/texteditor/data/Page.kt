package com.nbow.texteditor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "page")
data class Page(
    @PrimaryKey(autoGenerate = true)
    val id_page : Long,
    var id_History: Long,
    val data : String,
)