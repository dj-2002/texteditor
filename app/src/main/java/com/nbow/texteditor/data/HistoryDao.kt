package com.nbow.texteditor.data


import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE


@Dao
interface HistoryDao {
    @Transaction
    @Query("SELECT * FROM history")
    fun getHistoryWithPages():MutableList<HistoryWithPages>

    @Insert(onConflict = IGNORE)
    fun insertHistory(vararg history: History)

    @Query("SELECT * FROM history WHERE uriString LIKE :uriStr LIMIT 1")
    fun getHistoryByUriString(uriStr: String) : History

    @Insert(onConflict = IGNORE)
    fun insertPage(vararg page: Page)

    @Query("SELECT * FROM page")
    fun getPages():MutableList<Page>

    @Delete
    fun delete(history: History)

    @Query("delete  from page")
    fun deleteAllPages()

    @Query("delete  from history")
    fun deleteAllHistories()
}