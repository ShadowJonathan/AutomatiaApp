package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static String TAG = "REGISTRY";
    private Archive forArchive;
    private DbHelper db;
    private Updated u;

    Registry(Archive a, DbHelper db) {
        forArchive = a;
        this.db = db;
        u = new Updated("registry", a.name);
    }

    RegistryList getList() {
        return new RegistryList(db);
    }

    public void process(JSONArray a) {
        SQLiteDatabase DB = db.getWritableDatabase();
        for (int i = 0; i < a.length(); i++) {
            try {
                processOne(a.getJSONObject(i), DB);
            } catch (JSONException je) {
                Log.w(TAG, "process: JSONERROR", je);
            }
        }
        u.now();
    }

    private void processOne(JSONObject o, SQLiteDatabase DB) throws JSONException {
        ContentValues values = new ContentValues();
        String storyID = o.getString("storyID");
        values.put(RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE, forArchive.name);
        values.put(RegistryContract.RegEntry.COLUMN_NAME_STORYID, storyID);
        values.put(RegistryContract.RegEntry.COLUMN_NAME_DATA, o.toString());

        int id = (int) DB.insertWithOnConflict(
                RegistryContract.RegEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (id == -1) {
            DB.update(
                    RegistryContract.RegEntry.TABLE_NAME,
                    values,
                    RegistryContract.RegEntry.COLUMN_NAME_STORYID + "=?",
                    new String[]{storyID});
        }
    }

    public class RegistryList extends ArrayList<RegistryEntry> {
        RegistryList(DbHelper db) {
            SQLiteDatabase Db = db.getReadableDatabase();

            String[] projection = {
                    RegistryContract.RegEntry._ID,
                    RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE,
                    RegistryContract.RegEntry.COLUMN_NAME_DATA,
            };
            String selection = RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE + " = ?";
            String[] selectionArgs = {forArchive.name};

            Cursor cursor = Db.query(
                    CategoryContract.CatEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );


            while (cursor.moveToNext()) {
                try {
                    this.add(new RegistryEntry(cursor));
                } catch (JSONException je) {
                    Log.e(TAG, "RegistryList: JSON ERROR: ", je);
                }
            }
            cursor.close();
        }

        public Map<String, RegistryEntry> toMap() {
            Map<String, RegistryEntry> m = new HashMap<String, RegistryEntry>();
            for (RegistryEntry e : this) {
                m.put(e.storyID, e);
            }
            return m;
        }
    }

    public class RegistryEntry {
        public String rating;
        public Date updated;
        public int words;
        public boolean completed;
        public String langcode;
        public int chapters;
        public String authorID;
        public URL authorURL;
        public ArrayList<String> characters;
        public ArrayList<String> genre;
        public int favs;
        public String language;
        public String title;
        public URL url;
        public int follows;
        public String author;
        public ArrayList<String> ships;
        public String summary;
        public int reviews;
        public Date last_refresh;
        public String storyID;
        public Date published;

        RegistryEntry(Cursor cursor) throws JSONException {
            String data = cursor.getString(cursor.getColumnIndex(RegistryContract.RegEntry.COLUMN_NAME_DATA));
            JSONObject o = new JSONObject(data);

            if (o.has("rating"))
                rating = o.getString("rating");

            if (o.has("updated"))
                updated = Helper.parseDate(o.getString("updated"));

            if (o.has("words"))
                words = o.getInt("words");

            completed = o.has("completed") && o.getBoolean("completed");

            if (o.has("langcode"))
                langcode = o.getString("langcode");

            if (o.has("chapters"))
                chapters = o.getInt("chapters");

            if (o.has("authorId"))
                authorID = o.getString("authorId");

            try {
                if (o.has("authorUrl"))
                    authorURL = new URL(o.getString("authorUrl"));
            } catch (MalformedURLException me) {
                Log.w(TAG, "RegistryEntry: MALURL", me);
                authorURL = null;
            }

            if (o.has("characters"))
                characters = toList(o.getJSONArray("characters"));

            if (o.has("genre"))
                genre = toList(o.getJSONArray("genre"));

            if (o.has("favs"))
                favs = o.getInt("favs");

            if (o.has("language"))
                language = o.getString("language");

            if (o.has("title"))
                title = o.getString("title");

            try {
                if (o.has("url"))
                    url = new URL(o.getString("authorUrl"));
            } catch (MalformedURLException me) {
                Log.w(TAG, "RegistryEntry: MALURL", me);
                url = null;
            }

            if (o.has("follows"))
                follows = o.getInt("follows");

            if (o.has("author"))
                author = o.getString("author");

            if (o.has("ships"))
                ships = toList(o.getJSONArray("ships"));

            if (o.has("summary"))
                summary = o.getString("summary");

            if (o.has("reviews"))
                reviews = o.getInt("reviews");

            if (o.has("last_refreshed"))
                last_refresh = Helper.parseDate(o.getString("last_refreshed"));

            if (o.has("storyID"))
                storyID = o.getString("storyID");

            if (o.has("published"))
                published = Helper.parseDate(o.getString("published"));
        }

        private ArrayList<String> toList(JSONArray a) throws JSONException {
            ArrayList<String> l = new ArrayList<String>();
            for (int i = 0; i < a.length(); i++) {
                l.add(a.get(i).toString());
            }
            return l;
        }
    }
}
