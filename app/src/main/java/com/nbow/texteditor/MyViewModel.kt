package com.nbow.texteditor

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.nbow.texteditor.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.*
import java.lang.StringBuilder
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val fragmentList = MutableLiveData<MutableList<Fragment>>(arrayListOf())
    private val repository : Repository = Repository(application)
    private  var recentFileList = MutableLiveData(mutableListOf<RecentFile>())

    var currentTab : Int = -1
    var isWrap = false
    var isHistoryLoaded = MutableLiveData(false)

    private val TAG = "MyViewModel"

    init {

        loadHistory(application.applicationContext)
        loadRecentFile()
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        isWrap = preferences.getBoolean("word_wrap",true)

    }

    fun getRecentFileList(): LiveData<MutableList<RecentFile>> {
        return recentFileList
    }

    private fun loadRecentFile() {
        viewModelScope.launch(Dispatchers.IO) {
            recentFileList.postValue(repository.getRecentFileList())
            Log.e(TAG,"recent file list size ${recentFileList.value!!.size}")

        }
    }


    fun addHistories(context: Context){

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
            for(frag : Fragment in fragmentList.value!!){
                val editorFragment = frag as EditorFragment
                val uniqueFileName = editorFragment.getFileName() + (0..10000).random()
                val file = File(context.filesDir,uniqueFileName)
                if(!file.exists()) file.createNewFile()
                context.openFileOutput(uniqueFileName, Context.MODE_PRIVATE).use {
                    it.write(
                        editorFragment.getEditable()?.let
                            { it1 -> Utils.spannableToHtml(it1).toString().toByteArray() })
                }
                val history = History(0,editorFragment.getUri().toString(),uniqueFileName,editorFragment.getFileName(),editorFragment.hasUnsavedChanges.value?:true)

                Log.e(TAG, "saving new file to databse: file id ${history.historyId}")
                repository.addHistory(history)


            }
        }
    }

    fun loadHistory( context: Context){

        viewModelScope.launch(Dispatchers.IO) {

            val historyList:MutableList<History> = repository.getHistory()
            Log.e("view model","size history ${historyList.size} fragment list size : ${fragmentList.value!!.size}")

            for(history in historyList){
                val uri : Uri = Uri.parse(history.uriString)
                var data:StringBuilder = StringBuilder()


                context.openFileInput(history.fileName).bufferedReader().forEachLine { line ->
                    data.append(line+"\n")
                }

                val datafile  = DataFile(history.realFileName,uri.path!!,uri,Utils.htmlToSpannable(data.toString()))
                val frag = EditorFragment(datafile,getApplication(),history.hasUnsavedData)
                (fragmentList.value?: arrayListOf()).add(frag)
//                Log.e(TAG, "loadHistory: ${history.fileName} => hasUnsavedData ${history.hasUnsavedData} and frag unsaved data : ${frag.hasUnsavedChanges.value}")

            }
            isHistoryLoaded.postValue(true)
        }

    }



    fun getFragmentList(): LiveData<MutableList<Fragment>> {
        return fragmentList
    }

    fun setFragmentList(fragmentList : MutableList<Fragment>){
        this.fragmentList.value = fragmentList
    }

    fun addRecentFile(recentFile: RecentFile)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveToRecentFile(recentFile)
        }
    }

    fun deleteRecentFile(recentFile: RecentFile)
    {
        viewModelScope.launch(IO){
            repository.deleteRecentFile(recentFile)
        }
    }

    fun deleteAllRecentFile() {
        viewModelScope.launch(IO) {
            repository.deleteAllRecentFile()
        }
    }


}