package com.notes.colornotes.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.notes.colornotes.room.database.DatabaseBuilder

class TrashViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    fun updateDBForTrash(isTrash: Boolean, noteID: Int){
        DatabaseBuilder.getInstance(context).dao().updateForTrash(isTrash, noteID)
    }

    fun deleteNoteForTrash(noteID: Int){
        DatabaseBuilder.getInstance(context).dao().deleteBasedOnID(noteID)
    }

}