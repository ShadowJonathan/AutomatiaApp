package com.github.shadowjonathan.automatiaapp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.ffnet.Category;
import com.github.shadowjonathan.automatiaapp.ffnet.DbHelper;
import com.github.shadowjonathan.automatiaapp.ffnet.Updated;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.shadowjonathan.automatiaapp.ffnet.Category.Categories;

public class Modules {
    private static String TAG = "MODULES";
    protected DbHelper DB;
    private FFnet ffnet;
    private Comms C;
    private Context app_context;
    private String FileDir;
    private String CacheDir;
    private Handler UIHandler;

    Modules(Context context, Handler UIHandler) {
        this.UIHandler = UIHandler;
        app_context = context;
        FileDir = app_context.getFilesDir().getParent();
        CacheDir = FileDir + "/cache";
        Updated.bindDB(getDB());

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
        for (String s : new String[]{"anime", "book", "cartoon", "game", "misc", "play", "movie", "tv"}) {
            Category.getCategory(ffnet, s);
            Log.d(TAG, "Modules: Initialising cat " + s);
        }
    }

    void onReady() {

    }

    void onDestroy() {
        if (DB != null) {
            DB.close();
        }
    }

    public DbHelper getDB() {
        if (DB == null)
            DB = new DbHelper(app_context);
        return DB;
    }

    public class FFnet {
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

        public DbHelper getDB() {
            return Modules.this.getDB();
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

        void dumpStamps() {
            Log.d("", "dumpStamps: " + app_context.getFilesDir().getParent());
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

    }
}