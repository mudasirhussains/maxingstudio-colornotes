package com.notes.colornotes.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.notes.colornotes.room.database.DatabaseBuilder

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    fun updateDBForFavorite(isFav: Boolean, noteID: Int){
        DatabaseBuilder.getInstance(context).dao().updateForFavorite(isFav, noteID)
    }

}