package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.Modules;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Category {
    public static Map<String, Category> Categories = new HashMap<String, Category>();
    private static String TAG = "CATEGORY";
    public String name;
    private Map<String, Archive> Archives;
    private Modules.FFnet ffnet;
    private ArrayList<String> ArchiveNames;

    Category(Modules.FFnet ffnet, String name) {
        this.ffnet = ffnet;
        this.name = name;

        ArchiveNames = this.getArchiveNames();
        if (ArchiveNames.isEmpty()) {
            ffnet.sendMessage(new Helper.JSONConstructor()
                    .i("archive", "?")
                    .i("category", this.name)
            );
            Log.d(TAG, "Category: Requested Archives for " + this.name);
        }
        Log.d(TAG, "Category: Archive initialised for " + this.name);
    }

    public static Category getCategory(Modules.FFnet ffnet, String name) {
        if (Categories.containsKey(name))
            return Categories.get(name);
        Category cat = new Category(ffnet, name);
        Categories.put(name, cat);
        return cat;
    }

    public void onMessage(JSONObject o) {
        try {
            if (!o.optString("archive").isEmpty()) {
                Archive a = getArchive(o.optString("archive"));
                if (a == null) Log.w(TAG, "onMessage: MISSED MESSAGE: " + o);
                else a.onMessage(o);
            } else if (o.has("meta")) {
                if (o.optString("meta").equals("category")) {
                    final JSONObject O = o;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                processArchives(O.getJSONArray("data"));
                            } catch (JSONException je) {
                                Log.e(TAG, "onMessage_THREAD: JSONERROR", je);
                            }
                        }
                    }).start();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "onMessage: JSONERROR", je);
        }
    }

    public boolean hasArchive(String name) {
        if (ArchiveNames == null) {
            ArchiveNames = this.getArchiveNames();
        }

        return ArchiveNames.contains(name.replaceAll("\\s", "-").toLowerCase().trim());
    }

    private ArrayList<String> getArchiveNames() {
        SQLiteDatabase db = ffnet.getDB().getReadableDatabase();

        String[] projection = {
                CategoryContract.CatEntry._ID,
                CategoryContract.CatEntry.COLUMN_NAME_CATAGORY,
                CategoryContract.CatEntry.COLUMN_NAME_NAME
        };
        String selection = CategoryContract.CatEntry.COLUMN_NAME_CATAGORY + " = ?";
        String[] selectionArgs = {this.name};

        Cursor cursor = db.query(
                CategoryContract.CatEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        ArrayList<String> ArchiveNames = new ArrayList<String>();
        while (cursor.moveToNext()) {
            ArchiveNames.add(cursor.getString(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_NAME))
                    .replaceAll("\\s", "-")
                    .toLowerCase()
                    .trim()
            );
        }
        cursor.close();
        return ArchiveNames;
    }

    private void processArchives(JSONArray a) throws JSONException {
        ArrayList<JSONObject> list = Helper.makeObjects(a);
        SQLiteDatabase db = ffnet.getDB().getWritableDatabase();
        Log.d(TAG, this.name + ": processArchives: Processing...");
        for (JSONObject o : list) {
            processOneArchive(o, db);
        }
        Log.d(TAG, this.name + ": processArchives: Processed Archives!");
    }

    private void processOneArchive(JSONObject o, SQLiteDatabase DB) throws JSONException {
        ContentValues values = new ContentValues();
        String name = o.getString("name");
        String url = o.getString("url");
        int len = o.getInt("len");
        values.put(CategoryContract.CatEntry.COLUMN_NAME_CATAGORY, this.name);
        values.put(CategoryContract.CatEntry.COLUMN_NAME_NAME, name);
        values.put(CategoryContract.CatEntry.COLUMN_NAME_URL, url);
        values.put(CategoryContract.CatEntry.COLUMN_NAME_LEN, len);

        int id = (int) DB.insertWithOnConflict(
                CategoryContract.CatEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (id == -1) {
            DB.update(
                    CategoryContract.CatEntry.TABLE_NAME,
                    values,
                    CategoryContract.CatEntry.COLUMN_NAME_NAME + "=?",
                    new String[]{name}
            );
        }
    }

    Archive registerArchive(String name) {
        if (Archives.containsKey(name))
            return Archives.get(name);
        Archive a = new Archive(this, name, ffnet);
        Archives.put(name, a);
        return a;
    }

    public Archive getArchive(String name) {
        if (Archives.containsKey(name))
            return Archives.get(name);
        else
            return null;
    }
}
