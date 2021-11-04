package com.nbow.texteditor

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat

class Utils {

    private val REQUEST_PERMISSION_LOWER_THAN_11 = 1182
    private val TAG = "Utils"
    private var activity: AppCompatActivity? = null
    private var permission11Launcher: ActivityResultLauncher<Intent>? = null


    companion object{
        private val TAG = "Utils"
//        val heading = arrayListOf(2f,1.5f,1.17f,1f,0.83f,0.67f)
        val heading = arrayListOf(1.5f,1.4f,1.3f,1.2f,1.1f,1f)

        fun htmlToSpannable(data : String): Spanned {

//            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
//                val s =   Html.fromHtml(data, Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV)
//                Log.e(TAG, "htmlToSpannable: ${s}", )
//                return  s
//            }
//            else
//                return HtmlCompat.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY)
            return CustomHtmlCompact.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY,null,null)

        }

        fun spannableToHtml(data : Spanned): String {
                val s= CustomHtmlCompact.spannedtoHtml(data)
            Log.e(TAG, "spannableToHtml: $s", )
            return s;
        }

        fun convertValueToInt(charSeq: CharSequence?, defaultValue: Int): Int {
            if (null == charSeq) return defaultValue
            val nm = charSeq.toString()
            // XXX This code is copied from Integer.decode() so we don't
            // have to instantiate an Integer!
            var value: Int
            var sign = 1
            var index = 0
            val len = nm.length
            var base = 10
            if ('-' == nm[0]) {
                sign = -1
                index++
            }
            if ('0' == nm[index]) {
                //  Quick check for a zero by itself
                if (index == len - 1) return 0
                val c = nm[index + 1]
                if ('x' == c || 'X' == c) {
                    index += 2
                    base = 16
                } else {
                    index++
                    base = 8
                }
            } else if ('#' == nm[index]) {
                index++
                base = 16
            }
            return nm.substring(index).toInt(base) * sign
        }

    }

    constructor(activity: AppCompatActivity) {
        this.activity = activity
        permission11Launcher = activity.registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    Log.e(TAG, "permission granted on android 11")
                } else {
                    Log.e(TAG, "permission does not granted on android 11")
                }
            }
        }
    }

    fun queryName(resolver: ContentResolver, uri: Uri?): String {
        val returnCursor: Cursor = uri?.let { resolver.query(it, null, null, null, null) }!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    fun takePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION_LOWER_THAN_11
            )

        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION_LOWER_THAN_11
            )
        }
    }

    fun isStoragePermissionGranted():Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && activity!!.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.e(TAG, "Read write permission granted")
                 return true
            }
            return false
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

            if (activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && activity!!.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.e(TAG, "Read write permission granted")
                 return true
            }
            else
            {
                return false
            }
        }
        return true
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = activity!!.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }


}