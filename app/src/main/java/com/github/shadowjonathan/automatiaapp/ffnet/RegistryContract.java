package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public final class RegistryContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RegEntry.TABLE_NAME + " (" +
                    RegEntry._ID + " INTEGER PRIMARY KEY," +
                    RegEntry.COLUMN_NAME_ARCHIVE + " TEXT," +
                    RegEntry.COLUMN_NAME_STORYID + " TEXT UNIQUE," +
                    RegEntry.COLUMN_NAME_DATA + " TEXT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RegEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private RegistryContract() {
    }

    public static class RegEntry implements BaseColumns {
        public static final String TABLE_NAME = "registry";
        public static final String COLUMN_NAME_ARCHIVE = "archive";
        public static final String COLUMN_NAME_STORYID = "story";
        public static final String COLUMN_NAME_DATA = "data";
    }
}