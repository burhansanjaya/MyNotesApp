package com.dicoding.mynotesapp.db;

import android.provider.BaseColumns;

/*
    * digunakan untuk mempermudah akses nama tabel dan nama kolom di dalam database
    * kolom id sudah ada secara otomatis di dalam kelas BaseColumnS
 */
public class DatabaseContract {

    static String TABLE_NAME = "note";
    public static final class NoteColumns implements BaseColumns {
        public static String TITLE = "title";
        public static String DESCRIPTION = "description";
        public static String DATE = "date";
    }
}
