package com.github.shadowjonathan.automatiaapp.global;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

public class Updated {
    private static DbHelper DB;
    private String regarding;
    private String ID;

    public Updated(String regarding, String ID) {
        this.regarding = regarding;
        this.ID = ID;
    }

    public static void bindDB(DbHelper db) {
        DB = db;
    }

    public static ArrayList<UpdateTag> getAll() {
        ArrayList<UpdateTag> list = new ArrayList<UpdateTag>();
        Cursor cursor = DB.getReadableDatabase().query(
                UpdateContract.UdEntry.TABLE_NAME,
                new String[]{
                        UpdateContract.UdEntry.COLUMN_NAME_REGARDING,
                        UpdateContract.UdEntry.COLUMN_NAME_COMB_ID,
                        UpdateContract.UdEntry.COLUMN_NAME_DATE},
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String reg = cursor.getString(cursor.getColumnIndex(UpdateContract.UdEntry.COLUMN_NAME_REGARDING));
            String comb = cursor.getString(cursor.getColumnIndex(UpdateContract.UdEntry.COLUMN_NAME_COMB_ID));
            Date date = Helper.parseDate(cursor.getString(cursor.getColumnIndex(UpdateContract.UdEntry.COLUMN_NAME_DATE)));
            list.add(new UpdateTag(
                    reg,
                    comb.replace(reg + ":", ""),
                    date
            ));
        }
        cursor.close();

        return list;
    }

    private String combID() {
        return this.regarding + ":" + this.ID;
    }

    public Date when() {
        Cursor cursor = DB.getReadableDatabase().query(
                UpdateContract.UdEntry.TABLE_NAME,
                new String[]{UpdateContract.UdEntry.COLUMN_NAME_REGARDING,
                        UpdateContract.UdEntry.COLUMN_NAME_COMB_ID,
                        UpdateContract.UdEntry.COLUMN_NAME_DATE},
                UpdateContract.UdEntry.COLUMN_NAME_COMB_ID + "=?",
                new String[]{this.combID()},
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;
        Date d = Helper.parseDate(cursor.getString(cursor.getColumnIndex(UpdateContract.UdEntry.COLUMN_NAME_DATE)));
        cursor.close();
        return d;
    }

    public void now() {
        at(new Date());
    }

    public void at(Date d) {
        SQLiteDatabase db = DB.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UpdateContract.UdEntry.COLUMN_NAME_REGARDING, this.regarding);
        values.put(UpdateContract.UdEntry.COLUMN_NAME_COMB_ID, combID());
        values.put(UpdateContract.UdEntry.COLUMN_NAME_DATE, Helper.TSUtils.getISO8601(d));

        int id = (int) db.insertWithOnConflict(
                UpdateContract.UdEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (id == -1) {
            db.update(
                    UpdateContract.UdEntry.TABLE_NAME,
                    values,
                    UpdateContract.UdEntry.COLUMN_NAME_COMB_ID + "=?",
                    new String[]{combID()}
            );
        }
    }

    public static class UpdateTag {
        public String regarding;
        public String ID;
        public Date date;

        UpdateTag(String reg, String id, Date d) {
            regarding = reg;
            ID = id;
            date = d;
        }
    }
}
