package com.notes.colornotes.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.notes.colornotes.room.dao.NotesDao
import com.notes.colornotes.room.entity.NoteModel

@Database(
    entities = [NoteModel::class],
    version = 4,
    exportSchema = false
)
abstract class DatabaseBuilder : RoomDatabase() {

    abstract fun dao(): NotesDao

    companion object {
        private var instance: DatabaseBuilder? = null

        @Synchronized
        fun getInstance(ctx: Context): DatabaseBuilder {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, DatabaseBuilder::class.java,
                    "notes_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()

            return instance!!

        }

    }
}