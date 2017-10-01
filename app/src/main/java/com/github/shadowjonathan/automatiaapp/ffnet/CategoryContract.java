package com.github.shadowjonathan.automatiaapp.ffnet;

import android.provider.BaseColumns;

public final class CategoryContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CatEntry.TABLE_NAME + " (" +
                    CatEntry._ID + " INTEGER PRIMARY KEY," +
                    CatEntry.COLUMN_NAME_CATAGORY + " TEXT," +
                    CatEntry.COLUMN_NAME_NAME + " TEXT," +
                    CatEntry.COLUMN_NAME_URL + " TEXT," +
                    CatEntry.COLUMN_NAME_LEN + " INT)";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CatEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private CategoryContract() {
    }

    /* Inner class that defines the table contents */
    public static class CatEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME_CATAGORY = "cat";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_LEN = "len";
    }
}