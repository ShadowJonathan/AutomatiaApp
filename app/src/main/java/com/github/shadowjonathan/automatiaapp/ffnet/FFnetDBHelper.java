package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FFnetDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "FFnet.db";

    public FFnetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CategoryContract.SQL_CREATE_ENTRIES);
        db.execSQL(RegistryContract.SQL_CREATE_ENTRIES);
        db.execSQL(ArchiveContract.SQL_CREATE_ENTRIES);
        db.execSQL(PinContract.SQL_CREATE_ENTRIES);
        db.execSQL(StoryContract.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CategoryContract.SQL_DELETE_ENTRIES);
        db.execSQL(RegistryContract.SQL_DELETE_ENTRIES);
        db.execSQL(ArchiveContract.SQL_DELETE_ENTRIES);
        db.execSQL(PinContract.SQL_DELETE_ENTRIES);
        db.execSQL(StoryContract.SQL_DELETE_ENTRIES);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}