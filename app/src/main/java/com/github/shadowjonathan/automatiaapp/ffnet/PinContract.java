package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public final class PinContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PinEntry.TABLE_NAME + " (" +
                    PinEntry._ID + " INTEGER PRIMARY KEY," +
                    PinEntry.COLUMN_NAME_CATEGORY + " TEXT," +
                    PinEntry.COLUMN_NAME_ARCHIVE + " TEXT," +
                    PinEntry.COLUMN_NAME_PINNED + " BOOLEAN," +
                    "UNIQUE(" + PinEntry.COLUMN_NAME_CATEGORY + ", " + PinEntry.COLUMN_NAME_ARCHIVE + "))";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PinEntry.TABLE_NAME;

    private PinContract() {
    }

    public static class PinEntry implements BaseColumns {
        public static final String TABLE_NAME = "pinnable";
        public static final String COLUMN_NAME_CATEGORY = "cat";
        public static final String COLUMN_NAME_ARCHIVE = "archive";
        public static final String COLUMN_NAME_PINNED = "pinned";
    }
}