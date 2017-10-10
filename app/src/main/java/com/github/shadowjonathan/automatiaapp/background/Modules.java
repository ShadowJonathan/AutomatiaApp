package com.github.shadowjonathan.automatiaapp.background;

import android.content.Context;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.ffnet.Category;
import com.github.shadowjonathan.automatiaapp.ffnet.FFnetDBHelper;
import com.github.shadowjonathan.automatiaapp.ffnet.Stamps;
import com.github.shadowjonathan.automatiaapp.global.GlobalDBhelper;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.Updated;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.shadowjonathan.automatiaapp.ffnet.Category.Categories;

public class Modules {
    private static String TAG = "MODULES";
    protected GlobalDBhelper GDB;
    private FFnet ffnet;
    private Comms C;
    private Context app_context;
    private String FileDir;
    private String CacheDir;

    public Modules(Context context) {
        app_context = context;
        FileDir = app_context.getFilesDir().getParent();
        CacheDir = FileDir + "/cache";
        Updated.bindDB(getGDB());

        ffnet = new FFnet();
    }

    void BindComms(Comms c) {
        C = c;
    }

    void onMessage(JSONObject o) {
        try {
            if (o.has("orig")) {
                switch (o.getString("orig")) {
                    case "ffnet": {
                        ffnet.onMessage(o);
                    }
                }
            }
        } catch (JSONException ex) {
            Log.e("ONMESSAGE", "onMessage: " + o, ex);
        }
    }

    public void onPaste(String url) {
        Log.i(TAG, "onPaste: User pasted '" + url + "'");
    }

    public void onComms(Comms C) {
        this.C = C;

        ffnet.onComms(C);
    }

    void onReady() {

    }

    public boolean isOnline() {
        return C != null && C.isOnline();
    }

    void onDestroy() {
        ffnet.onDestroy();
        if (GDB != null) {
            GDB.close();
        }
    }

    public GlobalDBhelper getGDB() {
        if (GDB == null)
            GDB = new GlobalDBhelper(app_context);
        return GDB;
    }

    public FFnet getFFnet() {
        return ffnet;
    }

    public class FFnet {
        protected FFnetDBHelper DB;
        private String TAG = "MODULES.FFNET";
        /*
        private File stories;
        private File archives;
        private File cacheRoot;
        */

        FFnet() {
            /*
            stories = new File(CacheDir + "/ffnet/stories");
            archives = new File(CacheDir + "/ffnet/archives");
            cacheRoot = new File(CacheDir + "/ffnet/");
            if (!cacheRoot.exists())
                cacheRoot.mkdirs();
            if (!stories.exists())
                stories.mkdirs();
            if (!archives.exists())
                archives.mkdirs();

            Archive.init(archives);
            Category.init(cacheRoot);
            */
        }

        public Context getContext() {
            return app_context;
        }

        public FFnetDBHelper getDB() {
            if (DB == null)
                DB = new FFnetDBHelper(app_context);
            return DB;
        }

        void onMessage(JSONObject o) {
            if (o.has("get_stamps")) {
                this.dumpStamps();
            } else if (o.has("category")) {
                resolve(o).onMessage(o);
            }
        }

        public void sendMessage(JSONObject o) {
            try {
                o.put("orig", "ffnet");
            } catch (JSONException je) {
                Log.w(TAG, "sendMessage: JSONERROR", je);
            }
            C.send(o);
        }

        public boolean isOnline() {
            return Modules.this.isOnline();
        }

        void dumpStamps() {
            String temp = "";
            Stamps stamps = new Stamps();
            for (Updated.UpdateTag tag : Updated.getAll()) {
                stamps.input(tag.ID, tag.regarding, tag.date);
                temp += "\n" + tag.regarding + " -> " + tag.ID + ": " + Helper.TSUtils.getISO8601(tag.date);
            }
            Log.d("UPDATES_DUMP", temp);
            Log.d("UPDATES_DUMP", stamps.getJSON().toString());
            sendMessage(
                    new Helper.JSONConstructor()
                            .i("stamps", true)
                            .i("data", stamps.getJSON())
            );
        }

        Category resolve(JSONObject o) {
            if (!o.has("category"))
                return null;
            try {
                return Categories.get(o.getString("category"));
            } catch (JSONException je) {
                return null;
            }
        }

        public void onComms(Comms C) {
            for (String s : new String[]{"anime", "book", "cartoon", "game", "misc", "play", "movie", "tv"}) {
                Category.getCategory(ffnet, s);
                Log.d(TAG, "Initialising cat " + s);
            }
        }

        public void onDestroy() {
            if (DB != null) {
                DB.close();
            }
        }
    }
}