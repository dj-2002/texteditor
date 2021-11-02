package com.nbow.texteditor

import android.net.Uri
import android.text.Spanned

data class DataFile (
    var fileName : String,
    val filePath : String,
    val uri : Uri?,
    var data : Spanned,
    var isNote:Boolean =false
    ){
    val fileExtension = when(val index = fileName.lastIndexOf(".")){
        -1 -> String()
        else -> fileName.substring(index)
    }

}