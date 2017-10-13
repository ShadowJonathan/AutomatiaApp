package com.github.shadowjonathan.automatiaapp.global;

import android.provider.BaseColumns;

public final class DownloadContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DlEntry.TABLE_NAME + " (" +
                    DlEntry._ID + " INTEGER PRIMARY KEY," +
                    DlEntry.COLUMN_NAME_OWNER + " TEXT," +
                    DlEntry.COLUMN_NAME_SYSTEM_ID + " LONG UNIQUE," +
                    DlEntry.COLUMN_NAME_STATUS + " BOOLEAN," +
                    DlEntry.COLUMN_NAME_OWN_ID + " TEXT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DlEntry.TABLE_NAME;

    private DownloadContract() {
    }

    public static class DlEntry implements BaseColumns {
        public static final String TABLE_NAME = "downloads";
        public static final String COLUMN_NAME_OWNER = "owner";
        public static final String COLUMN_NAME_SYSTEM_ID = "id";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_OWN_ID = "own_ID";
    }
}
