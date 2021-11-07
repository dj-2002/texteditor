package com.nbow.texteditor.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity( tableName = "note")
data class Note(
    @PrimaryKey
    val fileName:String,
    val font:String = "default",
    val textSize:Float = 16f

)