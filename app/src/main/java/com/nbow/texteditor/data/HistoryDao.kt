package com.nbow.texteditor.data


import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE


@Dao
interface HistoryDao {

    @Insert(onConflict = IGNORE)
    fun insertHistory(vararg history: History)

    @Query("SELECT * FROM history WHERE uriString LIKE :uriStr LIMIT 1")
    fun getHistoryByUriString(uriStr: String) : History


    @Delete
    fun delete(history: History)


    @Query("delete  from history")
    fun deleteAllHistories()

    @Query("SELECT * FROM history")
    fun getAllHistory() : MutableList<History>
}