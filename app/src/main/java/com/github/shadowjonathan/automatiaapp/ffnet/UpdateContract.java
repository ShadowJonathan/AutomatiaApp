package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public final class UpdateContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UdEntry.TABLE_NAME + " (" +
                    UdEntry._ID + " INTEGER PRIMARY KEY," +
                    UdEntry.COLUMN_NAME_REGARDING + " TEXT," +
                    UdEntry.COLUMN_NAME_COMB_ID + " TEXT UNIQUE," +
                    UdEntry.COLUMN_NAME_DATE + " TEXT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UdEntry.TABLE_NAME;

    private UpdateContract() {
    }

    public static class UdEntry implements BaseColumns {
        public static final String TABLE_NAME = "updated";
        public static final String COLUMN_NAME_REGARDING = "regarding";
        public static final String COLUMN_NAME_COMB_ID = "id";
        public static final String COLUMN_NAME_DATE = "date";
    }
}
