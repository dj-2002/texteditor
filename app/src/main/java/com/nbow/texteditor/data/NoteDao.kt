package com.nbow.texteditor.data

import androidx.room.*

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(vararg note: Note)

    @Query("SELECT * FROM note WHERE fileName LIKE :name LIMIT 1")
    fun getNoteByName(name: String) : Note


    @Delete
    fun delete(note: Note)


    @Query("delete  from  note")
    fun deleteAllNotes()

    @Query("SELECT * FROM note")
    fun getAllNotes() : MutableList<Note>
}