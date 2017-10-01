package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public final class ArchiveContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ArchEntry.TABLE_NAME + " (" +
                    ArchEntry._ID + " INTEGER PRIMARY KEY," +
                    ArchEntry.COLUMN_NAME_ARCHIVE + " TEXT," +
                    ArchEntry.COLUMN_NAME_NAME + " TEXT," +
                    ArchEntry.COLUMN_NAME_URL + " TEXT UNIQUE," +
                    ArchEntry.COLUMN_NAME_LEN + " INT," +
                    ArchEntry.COLUMN_NAME_AMOUNT + " INT," +
                    ArchEntry.COLUMN_NAME_EARLIEST + " TEXT," +
                    ArchEntry.COLUMN_NAME_LATEST + " TEXT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ArchEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ArchiveContract() {
    }

    public static class ArchEntry implements BaseColumns {
        public static final String TABLE_NAME = "archives";
        public static final String COLUMN_NAME_ARCHIVE = "archive";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_LEN = "len";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_EARLIEST = "earliest";
        public static final String COLUMN_NAME_LATEST = "latest";
    }
}