

package com.nbow.texteditor.data
import androidx.annotation.FontRes
import androidx.room.Entity
import androidx.room.PrimaryKey


import androidx.room.Index
import java.lang.StringBuilder

@Entity(indices = [Index(value = ["uriString"], unique = true)] , tableName = "history")
data class History(
        @PrimaryKey(autoGenerate = true)
        val historyId: Long,             //file id
        val uriString: String?,
        val fileName:String,
        val realFileName:String,
        val hasUnsavedData : Boolean,
        val font:String = "default",
        val textSize:Float = 16f

)

