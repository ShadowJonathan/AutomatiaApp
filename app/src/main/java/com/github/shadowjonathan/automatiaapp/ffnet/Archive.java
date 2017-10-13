package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.background.Modules;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.Updated;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.github.shadowjonathan.automatiaapp.ffnet.Category.Categories;

public class Archive {
    private static Map<String, Archive> Archives = new HashMap<String, Archive>();
    private static String TAG = "ARCHIVE";
    public String name;
    public Registry reg;
    protected Category Cat;
    private Modules.FFnet ffnet;
    private Updated u;
    private Pinned p;
    private boolean refreshing = false;
    private RegistryUpdateCallback regListener = null;

    Archive(Category cat, String id, Modules.FFnet ffnet) {
        name = id;
        Cat = cat;
        this.ffnet = ffnet;
        reg = new Registry(this, ffnet.getDB());
        u = new Updated("archive", this.name);
        p = new Pinned(this);

        if (getInfo() == null)
            ffnet.sendMessage(getBase().i("info", true));
    }

    @Nullable
    public static Archive getArchive(String cat, String name) {
        Log.d(TAG, "getArchive: '" + cat + "' -> '" + name + "'");
        if (!Categories.containsKey(cat)) {
            Log.w("ARCHIVE_GET", cat + " DOES NOT EXIST");
            return null;
        }

        name = name.replaceAll("\\s", "-").toLowerCase();
        Category Cat = Categories.get(cat);
        if (!Cat.hasArchive(name)) {
            Log.w(TAG, "getArchive: CAT " + cat + " DOES NOT HAVE " + name);
            return null;
        }
        Archive a = Cat.registerArchive(name);
        Archives.put(a.makeID(), a);
        return a;
    }

    public static Archive getArchive(String ID) {
        return Archives.get(ID);
    }

    public boolean togglePin() {
        boolean state = p.isPinned();
        p.setPinned(!state);
        return !state;
    }

    private Helper.JSONConstructor getBase() {
        return new Helper.JSONConstructor()
                .i("archive", true)
                .i("a_url", this.Cat.name + "/" + this.name)
                .i("category", this.Cat.name);
    }

    public boolean refresh() {
        if (!refreshing) {
            getRegistry();
            refreshing = true;
        } else
            return false;
        return true;
    }

    public void getRegistry() {
        Log.d(TAG, "getRegistry: LOADING REGISTRY FOR " + name);
        ffnet.sendMessage(getBase()
                .i("getreg", true)
        );
    }

    public void getRegistry(RegistryUpdateCallback r) {
        regListener = r;
        getRegistry();
        if (ffnet.isOnline()) {
            regListener.onUpdate("Contacting server...");
        } else {
            regListener.onUpdate("Request queued, please come online.");
        }
    }

    public String getViewableName() {
        return Cat.getRef(this).name;
    }

    public void onMessage(final JSONObject o) throws JSONException {
        if (o.has("registry")) {
            if (!o.optBoolean("registry", true))
                getRegistry();
            else {
                refreshing = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject rawreg = o.getJSONObject("registry");
                            ArrayList<JSONObject> reglist = new ArrayList<JSONObject>();
                            for (Iterator<String> it = rawreg.keys(); it.hasNext(); ) {
                                reglist.add(rawreg.getJSONObject(it.next()));
                            }
                            if (regListener != null)
                                regListener.onUpdate("Processing...");
                            reg.process(reglist);

                            if (regListener != null)
                                regListener.run();
                        } catch (JSONException je) {
                            Log.e(TAG, "onMessage: MESSAGE_JSONERROR", je);
                        }
                    }
                }).start();
            }
        } else if (o.has("meta")) {
            if (o.optBoolean("initialised"))
                processInfo(o);
            else
                ffnet.sendMessage(getBase()
                        .i("getinfo", true)
                );
        } else if (o.has("registry_update")) {
            if (regListener != null) {
                regListener.onUpdate(o.optString("registry_update", ""));
            }
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
                ArchiveContract.ArchEntry.COLUMN_NAME_URL + " LIKE ?",
                new String[]{("%" + this.Cat.name + "/" + this.name + "%")},
                null, null, null, null);
        if (cursor == null || cursor.getCount() == 0)
            return null;
        ArchiveInfo ai = new ArchiveInfo(cursor);
        cursor.close();

        return ai;
    }

    public boolean isRef(Category.ArchiveRef af) {
        return af.url.toLowerCase().contains(this.name.toLowerCase());
    }

    public Category.ArchiveRef getRef() {
        return Cat.getRef(this);
    }

    public String makeID() {
        return Cat.name.toLowerCase() + ">" + this.name.toLowerCase();
    }

    public static abstract class RegistryUpdateCallback implements Runnable {
        public abstract void onUpdate(String text);
    }

    public static class Pinned {
        private static FFnetDBHelper DB;
        private Archive regarding;

        public Pinned(Archive regarding) {
            this.regarding = regarding;
        }

        public static void bindDB(FFnetDBHelper db) {
            DB = db;
        }

        public static ArrayList<Archive> getAll() {
            ArrayList<Archive> list = new ArrayList<Archive>();
            Cursor cursor = DB.getReadableDatabase().query(
                    PinContract.PinEntry.TABLE_NAME,
                    new String[]{
                            PinContract.PinEntry.COLUMN_NAME_CATEGORY,
                            PinContract.PinEntry.COLUMN_NAME_ARCHIVE,
                            PinContract.PinEntry.COLUMN_NAME_PINNED
                    },
                    null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(PinContract.PinEntry.COLUMN_NAME_PINNED)) == 1) {
                    String cat = cursor.getString(cursor.getColumnIndex(PinContract.PinEntry.COLUMN_NAME_CATEGORY));
                    String archive = cursor.getString(cursor.getColumnIndex(PinContract.PinEntry.COLUMN_NAME_ARCHIVE));
                    list.add(
                            Categories.get(cat)
                                    .registerArchive(archive)
                    );
                }
            }
            cursor.close();


            return list;
        }

        public boolean isPinned() {
            Cursor cursor = DB.getReadableDatabase().query(
                    PinContract.PinEntry.TABLE_NAME,
                    new String[]{
                            PinContract.PinEntry.COLUMN_NAME_CATEGORY,
                            PinContract.PinEntry.COLUMN_NAME_ARCHIVE,
                            PinContract.PinEntry.COLUMN_NAME_PINNED
                    },
                    PinContract.PinEntry.COLUMN_NAME_CATEGORY + "=? AND " + PinContract.PinEntry.COLUMN_NAME_ARCHIVE + "=?",
                    new String[]{regarding.Cat.name, regarding.name},
                    null, null, null, null);
            if (cursor != null && cursor.getCount() == 1)
                cursor.moveToFirst();
            else
                return false;
            boolean yes = cursor.getInt(cursor.getColumnIndex(PinContract.PinEntry.COLUMN_NAME_PINNED)) == 1;
            cursor.close();
            return yes;
        }

        public void setPinned(boolean pinned) {
            SQLiteDatabase db = DB.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(PinContract.PinEntry.COLUMN_NAME_CATEGORY, regarding.Cat.name);
            values.put(PinContract.PinEntry.COLUMN_NAME_ARCHIVE, regarding.name);
            values.put(PinContract.PinEntry.COLUMN_NAME_PINNED, pinned ? 1 : 0);

            int id = (int) db.insertWithOnConflict(
                    PinContract.PinEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
            if (id == -1) {
                db.update(
                        PinContract.PinEntry.TABLE_NAME,
                        values,
                        PinContract.PinEntry.COLUMN_NAME_CATEGORY + "=? AND " + PinContract.PinEntry.COLUMN_NAME_ARCHIVE + "=?",
                        new String[]{regarding.Cat.name, regarding.name}
                );
            }
        }
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
            cursor.moveToFirst();
            Log.d(TAG, "ArchiveInfo: " + cursor.getCount() + " [" + TextUtils.join(", ", cursor.getColumnNames()) + "]");
            cursor.moveToFirst();
            archive = cursor.getString(0);
            name = cursor.getString(1);
            url = cursor.getString(2);
            len = cursor.getInt(3);
            amount = cursor.getInt(4);
            earliest = Helper.parseDate(cursor.getString(5));
            latest = Helper.parseDate(cursor.getString(6));
        }
    }
}

