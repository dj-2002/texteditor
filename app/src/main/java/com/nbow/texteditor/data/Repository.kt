package com.nbow.texteditor.data


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Transaction
import java.io.File


class Repository(application: Application) {

    val application:Application=application
    val TAG = "Repository"

    val database: MyDatabase
    val historyDao:HistoryDao

    init {
        database=  MyDatabase.getDatabase(application)
        historyDao =database.HistoryDao()
    }

    suspend fun getHistory():MutableList<History>{


        var listOfHistory:MutableList<History> = arrayListOf()
        listOfHistory = historyDao.getAllHistory()
        return  listOfHistory
    }

    @Transaction
    suspend fun  addHistory(history:History)
    {
        historyDao.insertHistory(history)

    }

    suspend fun deleteAllHistory(context:Context) {
        historyDao.deleteAllHistories()
        
        val files = context.fileList()
        for (name in files)
        {
            Log.e(TAG, "deleteAllHistory: deleting$name", )
            File(context.filesDir,name).delete()
        }

        Log.e(TAG,"Deleting all files from database")
    }



}