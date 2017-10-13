package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.background.Modules;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.Updated;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registry {
    private static String TAG = "REGISTRY";
    private static RegistryList cache;
    private static Map<String, RegistryEntry> map_cache;
    private Archive forArchive;
    private FFnetDBHelper db;
    private Updated u;

    Registry(Archive a, FFnetDBHelper db) {
        forArchive = a;
        this.db = db;
        u = new Updated("registry", a.name);
    }

    private static RegistryList getCache() {
        if (cache == null)
            cache = new RegistryList(Modules.ffnet.getDB(), null);
        return cache;
    }

    public static Map<String, RegistryEntry> getMapCache() {
        if (map_cache == null)
            map_cache = getCache().toMap();
        return map_cache;
    }

    public static RegistryEntry requestStorySearch(String ID) {
        Log.d(TAG, "requestStorySearch: LOOKING FOR STORY WITH '" + ID + "'");
        RegistryEntry temp = getMapCache().get(ID);
        if (temp == null)
            Log.d(TAG, "requestStorySearch: CANT FIND '" + ID + "'");
        return temp;
    }

    private static void register(RegistryEntry re, FFnetDBHelper db) throws JSONException {
        SQLiteDatabase DB = db.getWritableDatabase();
        JSONObject o = re.original;
        String storyID = o.getString("storyID");
        ContentValues values = new ContentValues();
        values.put(RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE, re.archive);
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

    public RegistryList getList() {
        return new RegistryList(db, forArchive);
    }

    public void process(List<JSONObject> a) {
        SQLiteDatabase DB = db.getWritableDatabase();
        for (JSONObject o : a) {
            try {
                processOne(o, DB);
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

    public static class RegistryList extends ArrayList<RegistryEntry> {
        RegistryList(FFnetDBHelper db, Archive forArchive) {
            SQLiteDatabase Db = db.getReadableDatabase();

            String[] projection = {
                    RegistryContract.RegEntry._ID,
                    RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE,
                    RegistryContract.RegEntry.COLUMN_NAME_DATA,
            };

            String selection = null;
            String[] selectionArgs = null;
            if (forArchive != null) {
                selection = RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE + " = ?";
                selectionArgs = new String[]{forArchive.name};
            }

            Cursor cursor = Db.query(
                    RegistryContract.RegEntry.TABLE_NAME,
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

    public static class RegistryEntry {
        public String archive;
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
        protected JSONObject original;

        RegistryEntry(Cursor cursor) throws JSONException {
            String data = cursor.getString(cursor.getColumnIndex(RegistryContract.RegEntry.COLUMN_NAME_DATA));
            JSONObject o = new JSONObject(data);

            archive = cursor.getString(cursor.getColumnIndex(RegistryContract.RegEntry.COLUMN_NAME_ARCHIVE));

            processJSON(o);
        }

        RegistryEntry(JSONObject o) throws JSONException {
            archive = o.optString("archive", "");
            processJSON(o);
        }

        void processJSON(JSONObject o) throws JSONException {
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

            original = o;
        }

        private ArrayList<String> toList(JSONArray a) throws JSONException {
            ArrayList<String> l = new ArrayList<String>();
            for (int i = 0; i < a.length(); i++) {
                l.add(a.get(i).toString());
            }
            return l;
        }

        public String fullURL() {
            return "http://www.fanfiction.net/s/" + storyID + "/";
        }

        public void register(FFnetDBHelper db) {
            try {
                Registry.register(this, db);
            } catch (JSONException je) {
                Log.e(TAG, "register: JSON ERROR", je);
            }
        }
    }
}
