package com.nbow.texteditorpro

import android.net.Uri
import android.text.Spanned

data class DataFile (
    var fileName : String,
    val filePath : String,
    val uri : Uri?,
    var data : Spanned,
    var isNote: Boolean = true,
    var textSize: Float = 16f,
    var font:String = "default",

    ){
    val fileExtension = when(val index = fileName.lastIndexOf(".")){
        -1 -> String()
        else -> fileName.substring(index)
    }

}