

package com.nbow.texteditorpro.data
import androidx.room.Entity
import androidx.room.PrimaryKey


import androidx.room.Index

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

