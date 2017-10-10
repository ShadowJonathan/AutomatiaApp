package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.background.Modules;
import com.github.shadowjonathan.automatiaapp.global.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Category {
    public static Map<String, Category> Categories = new HashMap<String, Category>();
    private static Map<String, String> RealNames = new HashMap<String, String>();
    private static String TAG = "CATEGORY";

    static {
        RealNames.put("anime", "Anime/Manga");
        RealNames.put("book", "Books");
        RealNames.put("cartoon", "Cartoons");
        RealNames.put("game", "Games");
        RealNames.put("misc", "Misc");
        RealNames.put("play", "Plays/Musicals");
        RealNames.put("movie", "Movies");
        RealNames.put("tv", "TV");
    }

    public String name;
    private Map<String, Archive> Archives = new HashMap<String, Archive>();
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

    public static Category getCategory(String name) {
        if (Categories.containsKey(name))
            return Categories.get(name);
        else
            return null;
    }

    public static List<Category> getList() {
        return new ArrayList<Category>(Categories.values());
    }

    public static String findCat(String archive) {
        for (Category cat : Categories.values()) {
            if (cat.hasArchive(archive))
                return cat.name;
        }
        return null;
    }

    public String getViewableName() {
        return RealNames.get(this.name);
    }

    public void onMessage(JSONObject o) {
        try {
            if (!o.optString("archive").isEmpty()) {
                if (hasArchive(o.optString("archive"))) {
                    Archive a = registerArchive(o.optString("archive"));
                    if (a != null) {
                        a.onMessage(o);
                        return;
                    }
                }
                Log.w(TAG, "onMessage: MISSED MESSAGE: " + o);
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
        if (ArchiveNames == null || ArchiveNames.isEmpty()) {
            Log.v(TAG, "ArchiveNames is empty");
            ArchiveNames = this.getArchiveNames();
        }

        Log.v(TAG, "hasArchive: " + ArchiveNames);

        return ArchiveNames.contains(name.replaceAll("\\s", "-").toLowerCase().trim());
    }

    private ArrayList<String> getArchiveNames() {
        SQLiteDatabase db = ffnet.getDB().getReadableDatabase();

        String[] projection = {
                CategoryContract.CatEntry._ID,
                CategoryContract.CatEntry.COLUMN_NAME_CATAGORY,
                CategoryContract.CatEntry.COLUMN_NAME_URL
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
            //Log.v(TAG, "getArchiveNames: getting url "+cursor.getString(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_URL)));
            Matcher m = Pattern.compile("/?(\\w+?)/(.*?)/?", Pattern.CASE_INSENSITIVE).matcher(cursor.getString(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_URL)));
            m.matches();
            ArchiveNames.add(m.group(2)
                    .replaceAll("\\s", "-")
                    .toLowerCase()
                    .trim()
            );
        }
        cursor.close();
        return ArchiveNames;
    }

    public ArrayList<ArchiveRef> getArchives() {
        SQLiteDatabase db = ffnet.getDB().getReadableDatabase();

        String[] projection = {
                CategoryContract.CatEntry._ID,
                CategoryContract.CatEntry.COLUMN_NAME_CATAGORY,
                CategoryContract.CatEntry.COLUMN_NAME_NAME,
                CategoryContract.CatEntry.COLUMN_NAME_URL,
                CategoryContract.CatEntry.COLUMN_NAME_LEN,
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

        ArrayList<ArchiveRef> Archives = new ArrayList<ArchiveRef>();
        while (cursor.moveToNext()) {
            Archives.add(new ArchiveRef(
                            cursor.getString(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_URL)),

                            cursor.getString(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_NAME)),

                            cursor.getInt(cursor.getColumnIndex(CategoryContract.CatEntry.COLUMN_NAME_LEN))
                    )
            );
        }
        cursor.close();
        Collections.sort(Archives);
        return Archives;
    }

    private void processArchives(JSONArray a) throws JSONException {
        ArrayList<JSONObject> list = Helper.makeObjects(a);
        SQLiteDatabase db = ffnet.getDB().getWritableDatabase();
        Log.d(TAG, this.name + ": processArchives: Processing...");
        db.beginTransaction();
        for (JSONObject o : list) {
            processOneArchive(o, db);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
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

    public ArchiveRef getRef(Archive a) {
        for (ArchiveRef ar : getArchives()) {
            if (a.isRef(ar)) {
                return ar;
            }
        }
        return null;
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

    public class ArchiveRef implements Comparable<ArchiveRef> {
        public String url;
        public String name;
        public int len;

        ArchiveRef(String url, String name, int len) {
            this.url = url;
            this.name = name;
            this.len = len;
        }

        public Archive getArchive() {
            Log.d(TAG, "getArchive: '" + url + "'");
            Matcher m = Pattern.compile("/?(\\w+?)/(\\S*?)/?", Pattern.CASE_INSENSITIVE).matcher(url);
            m.matches();
            return Archive.getArchive(m.group(1), m.group(2));
        }

        @Override
        public int compareTo(@NonNull ArchiveRef archiveRef) {
            return archiveRef.len - this.len;
        }
    }
}
