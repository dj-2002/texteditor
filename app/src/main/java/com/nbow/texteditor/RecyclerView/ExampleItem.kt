package com.nbow.texteditor.RecyclerView

import java.io.File

data class ExampleItem(

     var fileName:String,
     var date:String,
     var fileSize:String,
     var file:File,
     var checkbox: Boolean = false
) {

}
