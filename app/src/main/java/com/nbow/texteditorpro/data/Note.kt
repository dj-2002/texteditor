package com.nbow.texteditorpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "note")
data class Note(
    @PrimaryKey
    val fileName:String,
    val font:String = "default",
    val textSize:Float = 16f

)