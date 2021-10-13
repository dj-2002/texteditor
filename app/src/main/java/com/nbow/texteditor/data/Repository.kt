package com.nbow.texteditor.data


import android.app.Application
import android.util.Log
import androidx.room.Transaction


class Repository(application: Application) {

    val application:Application=application
    val TAG = "Repository"

    val database: MyDatabase
    val historyDao:HistoryDao
    val recentFileDao:RecentFileDao

    init {
        database=  MyDatabase.getDatabase(application)
        historyDao =database.HistoryDao()
        recentFileDao=database.RecentFileDao()
    }

    suspend fun getHistory():MutableList<HistoryWithPages>{

//        var listOfHistory:MutableList<History> = arrayListOf()
//        listOfHistory = historyDao.getAll() as MutableList<History>
        var listOfHistory:MutableList<HistoryWithPages> = arrayListOf()

        listOfHistory = historyDao.getHistoryWithPages()
        Log.e(TAG,"${listOfHistory.size}")
        //Log.e(TAG, "getHistory: history ${listOfHistory.toString()}")
        return  listOfHistory
    }

    @Transaction
    suspend fun  addHistory(history:History,pages:MutableList<Page>)
    {
        Log.e(TAG, "addFile to database: size of page : ${pages.size}")
        val beforeHistory = historyDao.getHistoryByUriString(history.uriString)
//        Log.e(TAG, "addHistory: ${history.fileName} -> history : $history")
        if(beforeHistory!=null){
//            Log.e(TAG, "addHistory: before history : $beforeHistory")
            return
        }
//        Log.e(TAG, "addHistory: before insert history")
        historyDao.insertHistory(history)
        val afterHistory = historyDao.getHistoryByUriString(history.uriString)
//        Log.e(TAG, "addHistory: after getting history")

        pages.forEach{
//            Log.e(TAG, "addHistory: history id : ${afterHistory}")
            it.id_History = afterHistory.historyId
//            Log.e(TAG, "addHistory: before insert page")
            historyDao.insertPage(it)
//            Log.e(TAG, "addHistory: after insert page")
        }
        Log.e("repository","Saved")
    }

    suspend fun deleteAllHistory() {
        historyDao.deleteAllHistories()
        historyDao.deleteAllPages()
        Log.e(TAG,"Deleting all files from database")
    }

    suspend fun getRecentFileList():MutableList<RecentFile>{
        var listOfRecentFile = recentFileDao.getAll()
        val mutableListOfRecentFile = mutableListOf<RecentFile>()
        for(recentFile in listOfRecentFile.reversed()){
            mutableListOfRecentFile.add(recentFile)
        }

        Log.e(TAG,"size recentFilelist ${listOfRecentFile.size}")
//        if(!listOfRecentFile.isEmpty())
//            return  listOfRecentFile.reversed() as MutableList<RecentFile>
//        else{
//            return arrayListOf()
//        }
        return mutableListOfRecentFile

    }
    suspend fun saveToRecentFile(vararg  recentFile: RecentFile)
    {
        recentFileDao.insertAll(*recentFile)
        Log.e(TAG,"saving recent file")
    }
    suspend fun deleteRecentFile(recentFile: RecentFile)
    {
        recentFileDao.delete(recentFile)
    }

    fun deleteAllRecentFile() {
        recentFileDao.deleteAll()
    }

}