package com.github.shadowjonathan.automatiaapp.global;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class Downloads {

    private static GlobalDBhelper DB;

    public Downloads(GlobalDBhelper DB) {
        if (Downloads.DB == null) {
            Downloads.DB = DB;
        }
    }

    public Downloads() {
        if (Downloads.DB == null) throw new Error("NO DB");
    }

    public static void bindDB(GlobalDBhelper db) {
        DB = db;
    }

    public boolean hasID(long id) {
        Cursor cursor = DB.getReadableDatabase().query(
                DownloadContract.DlEntry.TABLE_NAME,
                new String[]{
                        DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID
                },
                DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID + "=?",
                new String[]{"" + id},
                null, null, null, null);
        if (cursor != null && cursor.getCount() == 1)
            cursor.moveToFirst();
        else
            return false;
        boolean yes = cursor.getLong(cursor.getColumnIndex(DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID)) == id;
        cursor.close();
        return yes;
    }

    public String ownerOf(long id) {
        Cursor cursor = DB.getReadableDatabase().query(
                DownloadContract.DlEntry.TABLE_NAME,
                new String[]{
                        DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID,
                        DownloadContract.DlEntry.COLUMN_NAME_OWNER,
                },
                DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID + "=?",
                new String[]{"" + id},
                null, null, null, null);
        if (cursor != null && cursor.getCount() == 1)
            cursor.moveToFirst();
        else
            return null;
        String owner = cursor.getString(cursor.getColumnIndex(DownloadContract.DlEntry.COLUMN_NAME_OWNER));
        cursor.close();
        return owner;
    }

    public String extraInfo(long id) {
        Cursor cursor = DB.getReadableDatabase().query(
                DownloadContract.DlEntry.TABLE_NAME,
                new String[]{
                        DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID,
                        DownloadContract.DlEntry.COLUMN_NAME_OWN_ID,
                },
                DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID + "=?",
                new String[]{"" + id},
                null, null, null, null);
        if (cursor != null && cursor.getCount() == 1) {
            Log.d("DOWNLOADS", "extraInfo: " + TextUtils.join(", ", cursor.getColumnNames()));
            cursor.moveToFirst();
        } else
            return null;
        String extra = cursor.getString(cursor.getColumnIndex(DownloadContract.DlEntry.COLUMN_NAME_OWN_ID));
        cursor.close();
        return extra;
    }

    public void reportDownloaded(long id) {
        if (!hasID(id))
            throw new Error("DOWNLOADS DOES NOT HAVE ID " + id);
        SQLiteDatabase db = DB.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DownloadContract.DlEntry.COLUMN_NAME_STATUS, 1);
        db.update(
                DownloadContract.DlEntry.TABLE_NAME,
                values,
                DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID + "=?",
                new String[]{"" + id}
        );
    }

    public void newEntry(String owner, long id, String extra) {
        SQLiteDatabase db = DB.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DownloadContract.DlEntry.COLUMN_NAME_OWNER, owner);
        values.put(DownloadContract.DlEntry.COLUMN_NAME_SYSTEM_ID, id);
        values.put(DownloadContract.DlEntry.COLUMN_NAME_STATUS, 0);
        values.put(DownloadContract.DlEntry.COLUMN_NAME_OWN_ID, extra);

        int ID = (int) db.insertWithOnConflict(
                DownloadContract.DlEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (ID == -1) {
            throw new Error("ENTRY ALREADY EXISTS: " + id);
        }
    }
}
