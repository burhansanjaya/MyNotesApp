package com.dicoding.mynotesapp.helper;

import android.database.Cursor;

import com.dicoding.mynotesapp.db.DatabaseContract;
import com.dicoding.mynotesapp.entity.Note;

import java.util.ArrayList;

/*
    * Metode queryAll() -> NoteHelper berfungsi mengambil data
    * Adapater menggunakan arraylist
    * Objek yang direutrn oleh queryAll() berupa Cursor,
    * maka harus mengonversi dari Cursor ke Arraylist class pembantu tersebut MappingHelper
 */
public class MappingHelper {

    public static ArrayList<Note> mapCursorToArrayList(Cursor notesCursor) {
        ArrayList<Note> notesList = new ArrayList<>();

        while (notesCursor.moveToNext()) {
            int id = notesCursor.getInt(notesCursor.getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID));
            String title = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE));
            String description = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION));
            String date = notesCursor.getString(notesCursor.getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE));
            notesList.add(new Note(id, title, description, date));
        }
        return notesList;
    }
}
