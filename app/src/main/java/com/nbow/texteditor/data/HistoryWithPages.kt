package com.nbow.texteditor.data

import androidx.room.Embedded
import androidx.room.Relation

data class HistoryWithPages(
    @Embedded val history: History,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "id_History"
    )
    val pages : MutableList<Page>
)
