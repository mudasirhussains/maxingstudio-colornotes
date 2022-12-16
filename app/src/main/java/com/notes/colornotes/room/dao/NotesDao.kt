package com.notes.colornotes.room.dao

import androidx.room.*
import com.notes.colornotes.room.entity.NoteModel

@Dao
interface NotesDao {
    @Query("SELECT * FROM color_notes_entity WHERE is_trash == 0 ORDER BY id DESC")
    fun getAllNotes(): List<NoteModel>

    @Query("UPDATE color_notes_entity SET is_trash = :isTrash WHERE id = :notesID")
    fun updateForTrash(isTrash : Boolean , notesID : Int)

    @Query("DELETE FROM color_notes_entity WHERE id = :notesID")
    fun deleteBasedOnID(notesID : Int)

    @Query("SELECT * FROM color_notes_entity WHERE is_trash == 1 ORDER BY id DESC")
    fun getAllTrashNotes(): List<NoteModel>

    @Query("UPDATE color_notes_entity SET is_favorite = :isFav WHERE id = :notesID")
    fun updateForFavorite(isFav : Boolean , notesID : Int)

    @Query("SELECT * FROM color_notes_entity WHERE is_favorite == 1 ORDER BY id DESC")
    fun getAllFavoriteNotes(): List<NoteModel>

    // Sorting
    @Query("SELECT * FROM color_notes_entity WHERE is_trash == 0 ORDER BY id ASC")
    fun getAllSortByOldest(): List<NoteModel>

    @Query("SELECT * FROM color_notes_entity WHERE is_trash == 0 ORDER BY title ASC")
    fun getAllSortByAZ(): List<NoteModel>

    @Query("SELECT * FROM color_notes_entity WHERE is_trash == 0 ORDER BY title DESC")
    fun getAllSortByZA(): List<NoteModel>
    // Sorting

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: NoteModel)

    @Delete
    fun deleteNote(note: NoteModel)

    @Query("UPDATE color_notes_entity SET is_trash = :isTrash WHERE id in (:idList)")
    fun updateForTrashMultiple(isTrash : Boolean , idList: List<Int>)

    @Query("DELETE FROM color_notes_entity WHERE id in (:idList)")
    fun deleteMultipleNotes(idList: List<Int>)
}