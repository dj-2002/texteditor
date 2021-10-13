package com.nbow.texteditor.data

import androidx.room.*

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recentfile")
    fun getAll(): List<RecentFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg recentFile: RecentFile)

    @Delete
    fun delete(recentFile: RecentFile)

    @Query("delete  from recentfile")
    fun deleteAll()
}