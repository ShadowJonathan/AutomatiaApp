package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public class StoryContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SEntry.TABLE_NAME + " (" +
                    SEntry._ID + " INTEGER PRIMARY KEY," +
                    SEntry.COLUMN_NAME_ID + " TEXT UNIQUE," +
                    SEntry.COLUMN_NAME_DOWNLOADED + " BOOLEAN," +
                    SEntry.COLUMN_NAME_LATEST_UPDATE + " TEXT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CategoryContract.CatEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private StoryContract() {
    }

    /* Inner class that defines the table contents */
    public static class SEntry implements BaseColumns {
        public static final String TABLE_NAME = "stories";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DOWNLOADED = "downloaded";
        public static final String COLUMN_NAME_LATEST_UPDATE = "latest";
    }
}
