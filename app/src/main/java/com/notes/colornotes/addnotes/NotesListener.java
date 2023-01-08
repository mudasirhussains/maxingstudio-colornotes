package com.notes.colornotes.addnotes;



import com.notes.colornotes.room.entity.NoteModel;


public interface NotesListener {
    void onNoteClicked(NoteModel noteModel, int position);
    void onNoteFavorite(NoteModel noteModel, int position);
    void onNoteOptionMenu(NoteModel noteModel, int position);
}
