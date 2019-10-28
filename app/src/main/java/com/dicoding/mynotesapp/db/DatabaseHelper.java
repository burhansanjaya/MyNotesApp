package com.dicoding.mynotesapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
    * DDL
    * menciptakan database dengan tabel yang dibutuhkan dan handle ketika terjadi perubahan skema pada tabel
    * menggunakan variabel yang ada pada DatabaseContract untuk mengisi kolom nama tabel
    * memanfaatkan kelas contract, maka akses nama tabel dan nama kolom tabel menjadi lebih mudah
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //nama database
    public static String DATABASE_NAME = "dbnoteapp";

    //versi database
    private static final int DATABASE_VERSION = 1;

    //query string create table
    private static final String SQL_CREATE_TABLE_NOTE = String.format("CREATE TABLE %s"
                    + " (%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL)",
            DatabaseContract.TABLE_NAME,
            DatabaseContract.NoteColumns._ID,
            DatabaseContract.NoteColumns.TITLE,
            DatabaseContract.NoteColumns.DESCRIPTION,
            DatabaseContract.NoteColumns.DATE
    );

    //menentukan nama database dan versi database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TABLE_NAME);
        onCreate(db);
    }
}
