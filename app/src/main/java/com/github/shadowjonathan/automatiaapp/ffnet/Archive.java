package com.github.shadowjonathan.automatiaapp.ffnet;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.background.Modules;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.Updated;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

import static com.github.shadowjonathan.automatiaapp.ffnet.Category.Categories;

public class Archive {
    private static Map<String, Archive> Archives;
    private static String TAG = "ARCHIVE";
    public String name;
    public Registry reg;
    private Category Cat;
    private Modules.FFnet ffnet;
    private Updated u;

    Archive(Category cat, String id, Modules.FFnet ffnet) {
        name = id;
        Cat = cat;
        this.ffnet = ffnet;
        reg = new Registry(this, ffnet.getDB());
        u = new Updated("archive", this.name);

        if (getInfo() == null)
            ffnet.sendMessage(getBase().i("info", true));
    }

    public static Archive getArchive(String cat, String name) {
        if (!Categories.containsKey(cat)) {
            Log.w("ARCHIVE_GET", cat + " DOES NOT EXIST");
            return null;
        }

        name = name.replaceAll("\\s", "-").toLowerCase();
        Category Cat = Categories.get(cat);
        if (!Cat.hasArchive(name))
            return null;
        Archive a = Cat.registerArchive(name);
        Archives.put(a.makeID(), a);
        return a;
    }

    public static Archive getArchive(String ID) {
        return Archives.get(ID);
    }

    private Helper.JSONConstructor getBase() {
        return new Helper.JSONConstructor()
                .i("archive", true)
                .i("a_url", this.Cat.name + "/" + this.name)
                .i("category", this.Cat.name);
    }

    public void getRegistry() {
        ffnet.sendMessage(getBase()
                .i("getreg", true)
        );
    }

    public void onMessage(JSONObject o) throws JSONException {
        if (o.has("registry")) {
            try {
                try {
                    if (!o.getBoolean("registry"))
                        getRegistry();
                } catch (JSONException ignored) {
                }
                reg.process(o.getJSONArray("registry"));
            } catch (JSONException je) {
                Log.e(TAG, "onMessage: MESSAGE_JSONERROR", je);
            }
        } else if (o.has("meta")) {
            if (o.optBoolean("initialised"))
                processInfo(o);
            else
                ffnet.sendMessage(getBase()
                        .i("getinfo", true)
                );
        }
    }

    private void processInfo(JSONObject o) throws JSONException {
        SQLiteDatabase DB = ffnet.getDB().getWritableDatabase();
        ContentValues values = new ContentValues();
        JSONObject meta = o.getJSONObject("meta");
        Date updated = Helper.parseDate(o.getString("updated"));
        String url = meta.getString("url");
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_ARCHIVE, this.name);
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_NAME, meta.getString("name"));
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_URL, url);
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_LEN, meta.getInt("len"));
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_AMOUNT, o.getInt("amount"));
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_EARLIEST, o.getString("earliest"));
        values.put(ArchiveContract.ArchEntry.COLUMN_NAME_LATEST, o.getString("latest"));

        int id = (int) DB.insertWithOnConflict(
                ArchiveContract.ArchEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (id == -1) {
            DB.update(
                    ArchiveContract.ArchEntry.TABLE_NAME,
                    values,
                    ArchiveContract.ArchEntry.COLUMN_NAME_URL + "=?",
                    new String[]{url}
            );
        }

        u.at(updated);
    }

    public ArchiveInfo getInfo() {
        Cursor cursor = ffnet.getDB().getReadableDatabase().query(
                ArchiveContract.ArchEntry.TABLE_NAME,
                new String[]{
                        ArchiveContract.ArchEntry.COLUMN_NAME_ARCHIVE,
                        ArchiveContract.ArchEntry.COLUMN_NAME_NAME,
                        ArchiveContract.ArchEntry.COLUMN_NAME_URL,
                        ArchiveContract.ArchEntry.COLUMN_NAME_LEN,
                        ArchiveContract.ArchEntry.COLUMN_NAME_AMOUNT,
                        ArchiveContract.ArchEntry.COLUMN_NAME_EARLIEST,
                        ArchiveContract.ArchEntry.COLUMN_NAME_LATEST
                },
                ArchiveContract.ArchEntry.COLUMN_NAME_URL + " LIKE '?'",
                new String[]{"%" + this.Cat.name + "/" + this.name + "%"},
                null, null, null, null);
        if (cursor == null)
            return null;
        ArchiveInfo ai = new ArchiveInfo(cursor);
        cursor.close();

        return ai;
    }

    public String makeID() {
        return Cat.name.toLowerCase() + ">" + this.name.toLowerCase();
    }

    public class ArchiveInfo {
        public String archive;
        public String name;
        public String url;
        public int len;
        public int amount;
        public Date earliest;
        public Date latest;

        ArchiveInfo(Cursor cursor) {
            archive = cursor.getString(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_ARCHIVE));
            name = cursor.getString(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_NAME));
            url = cursor.getString(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_URL));
            len = cursor.getInt(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_LEN));
            amount = cursor.getInt(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_AMOUNT));
            earliest = Helper.parseDate(cursor.getString(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_EARLIEST)));
            latest = Helper.parseDate(cursor.getString(cursor.getColumnIndex(ArchiveContract.ArchEntry.COLUMN_NAME_LATEST)));

        }
    }
}

