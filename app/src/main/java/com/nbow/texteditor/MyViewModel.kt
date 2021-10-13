package com.nbow.texteditor

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.nbow.texteditor.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
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
        loadHistory()
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


    fun addHistories(){

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
            for(frag : Fragment in fragmentList.value!!){
                val editorFragment = frag as EditorFragment
                Log.e(TAG, "saving new file to database: ${editorFragment.getFileName()} => hasUnsavedChanges ${editorFragment.hasUnsavedChanges.value}", )
                val history = History(0,editorFragment.getFileName(),editorFragment.getUri().toString(),editorFragment.hasUnsavedChanges.value?:true, Calendar.getInstance().time.toString())
                // TODO : list of pages to made
                Log.e(TAG, "saving new file to databse: file id ${history.historyId}")
                val pages = mutableListOf<Page>()
                for(data : String in editorFragment.getListOfPages()){
                    val page = Page(0,history.historyId,data)
                    Log.e(TAG, "saving new file to database: page-> page id ${page.id_History}")
                    pages.add(page)
                }
                repository.addHistory(history,pages)
            }
        }
    }

    fun loadHistory(){

        viewModelScope.launch(Dispatchers.IO) {

            val historyList:MutableList<HistoryWithPages> = repository.getHistory()
            Log.e("view model","size history ${historyList.size} fragment list size : ${fragmentList.value!!.size}")

            for(historyWithPages in historyList){
                val uri : Uri = Uri.parse(historyWithPages.history.uriString)
                val listOfPages:MutableList<String>  = arrayListOf()
                Log.e(TAG, "loading files: size of pages : ${historyWithPages.pages.size}" )
                for(page in historyWithPages.pages){
//                    Log.e(TAG, "loadHistory: page data : ${page.data}" )
                    listOfPages.add(page.data)
                }
                if(listOfPages.size==0){
                    listOfPages.add("")
                }
                val datafile  = DataFile(historyWithPages.history.fileName,uri.path!!,uri,listOfPages)

                val frag = EditorFragment(datafile,getApplication(),historyWithPages.history.hasUnsavedData)
                fragmentList.value?.add(frag)
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