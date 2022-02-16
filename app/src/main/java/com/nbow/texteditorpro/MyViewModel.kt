package com.nbow.texteditorpro

import android.app.Application
import android.content.Context
import android.net.Uri
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.nbow.texteditorpro.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.*
import java.lang.StringBuilder
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {
    var open = 1;
    private val fragmentList = MutableLiveData<MutableList<Fragment>>(arrayListOf())
    public var noteList:MutableList<Note>  = arrayListOf()
    private val repository : Repository = Repository(application)

    var currentTab : Int = -1
    var isWrap = false
    var isLineNumber = false

    var isHistoryLoaded = MutableLiveData(false)

    private val TAG = "MyViewModel"

    init {

        loadHistory(application.applicationContext)
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        isWrap = preferences.getBoolean("word_wrap",true)
        isLineNumber = preferences.getBoolean("line_number",false)

    }



    fun addHistories(context: Context){

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory(context)
            for(frag : Fragment in fragmentList.value!!){
                val editorFragment = frag as EditorFragment


                    val uniqueFileName = editorFragment.getFileName() + (0..10000).random()
                    val file = File(context.filesDir, uniqueFileName)
                    if (!file.exists()) file.createNewFile()
                    context.openFileOutput(uniqueFileName, Context.MODE_PRIVATE).use {
                        it.write(
                            editorFragment.getEditable()?.let
                            { it1 -> Utils.spannableToHtml(it1).toByteArray() })
                    }


                    var uriString: String? = null
                    if(editorFragment.getUri()!=null)
                        uriString=editorFragment.getUri().toString()
                        val history = History(
                        0,
                        uriString,
                        uniqueFileName,
                        editorFragment.getFileName(),
                        editorFragment.hasUnsavedChanges.value ?: true,
                            font = editorFragment.fontFamily,
                            textSize = editorFragment.getTextSize(context)
                    )

                    Log.e(TAG, "saving new file to databse: file id ${history.historyId}")
                    repository.addHistory(history)
                }

            }
        }


    fun loadHistory( context: Context){

        viewModelScope.launch(Dispatchers.IO) {

            val historyList:MutableList<History> = repository.getHistory()
            Log.e("view model","size history ${historyList.size} fragment list size : ${fragmentList.value!!.size}")

            for(history in historyList) {
                if (history.uriString != null) {
                    val uri: Uri = Uri.parse(history.uriString)
                    var data: StringBuilder = StringBuilder()

                    context.openFileInput(history.fileName).bufferedReader().forEachLine { line ->
                        data.append(line + "\n")
                    }
                    val datafile = DataFile(
                        history.realFileName,
                        uri.path!!,
                        uri,
                        Utils.htmlToSpannable(context,data.toString()),
                        font = history.font,
                        textSize =  history.textSize
                    )
                    val frag = EditorFragment(datafile, context, history.hasUnsavedData,this@MyViewModel)
                    (fragmentList.value ?: arrayListOf()).add(frag)
//                Log.e(TAG, "loadHistory: ${history.fileName} => hasUnsavedData ${history.hasUnsavedData} and frag unsaved data : ${frag.hasUnsavedChanges.value}")

                }
                else {
                    var data: StringBuilder = StringBuilder()

                    context.openFileInput(history.fileName).bufferedReader().forEachLine { line ->
                        data.append(line + "\n")
                    }
                    val datafile = DataFile(
                        history.realFileName,
                        "note/untitled.html",
                        null,
                        Utils.htmlToSpannable(context,data.toString()),
                        font = history.font,
                        textSize =  history.textSize
                    )
                    val frag = EditorFragment(datafile, context, history.hasUnsavedData,this@MyViewModel)
                    (fragmentList.value ?: arrayListOf()).add(frag)
                }
            }
            isHistoryLoaded.postValue(true)

            noteList = repository.getAllNotes()

        }

    }

    fun updateNoteList()
    {
        viewModelScope.launch(IO) {
            noteList = repository.getAllNotes()
        }
    }


    fun getFragmentList(): LiveData<MutableList<Fragment>> {
        return fragmentList
    }

    fun setFragmentList(fragmentList : MutableList<Fragment>){
        this.fragmentList.value = fragmentList
    }


    fun saveAsNote(context: Context,index:Int,name:String) {
        viewModelScope.launch(Dispatchers.IO) {

            //todo check exits
            val editorFragment = fragmentList.value!!.get(index) as EditorFragment
            Log.e(TAG, "saveAsNote: ${editorFragment.getEditable().toString()}", )
           // val uniqueFileName = editorFragment.getFileName()
            val dir = File(context.filesDir, "note")
            if (!dir.exists())
                dir.mkdir()
            val file = File(dir, name)
            if (!file.exists()) file.createNewFile()

            val note= Note(name,editorFragment.fontFamily,editorFragment.getTextSize(context))
            repository.saveNote(note)
            updateNoteList()

            file.bufferedWriter().use {
                it.write("${Utils.spannableToHtml(editorFragment.getEditable()?: SpannableStringBuilder(""))}")
            }

        }
    }

     fun renameNoteFile(context: Context,name:String,editorFragment:EditorFragment)
    {
        viewModelScope.launch(Dispatchers.IO) {

            val dir = File(context.filesDir, "note")
            if (!dir.exists())
                dir.mkdir()
            val file = File(dir, name)
            if (!file.exists()) file.createNewFile()

            val note= Note(name,editorFragment.fontFamily,editorFragment.getTextSize(context))
            val job = async {  repository.saveNote(note) }
            job.join()
            updateNoteList()

            file.bufferedWriter().use {
                it.write("${Utils.spannableToHtml(editorFragment.getEditable()?: SpannableStringBuilder(""))}")
            }

            val fileToDelete = File(dir,editorFragment.getFileName())
            if(fileToDelete.exists())
                fileToDelete.delete()
        }

    }

    fun getNoteByName(name:String): Note? {

        for (note in noteList)
        {
            if(note.fileName==name)
                return  note
        }
        return null
    }

    fun addNote(note: Note) {
        viewModelScope.launch(IO) {
            repository.saveNote(note)
            updateNoteList()
        }
    }


}