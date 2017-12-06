package com.github.shadowjonathan.automatiaapp.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.ffnet.Archive;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;
import com.github.shadowjonathan.automatiaapp.ffnet.FFnetDBHelper;
import com.github.shadowjonathan.automatiaapp.ffnet.Stamps;
import com.github.shadowjonathan.automatiaapp.ffnet.Story;
import com.github.shadowjonathan.automatiaapp.global.Downloads;
import com.github.shadowjonathan.automatiaapp.global.GlobalDBhelper;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.Updated;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.shadowjonathan.automatiaapp.ffnet.Category.Categories;

public class Modules {
    public static final String EXTRA_RETURN_MESSAGE = "EXTRA_RETURN_MESSAGE";
    public static FFnet ffnet;
    private static String TAG = "MODULES";
    protected GlobalDBhelper GDB;
    private Comms C;
    private Context app_context;

    public Modules(Context context) {
        Download.bindContext(context);
        app_context = context;
        Updated.bindDB(getGDB());
        Downloads.bindDB(getGDB());

        ffnet = new FFnet();
    }

    public void sendSnackBar(String message) {
        Intent it = new Intent("EVENT_SNACKBAR");

        if (!TextUtils.isEmpty(message))
            it.putExtra(EXTRA_RETURN_MESSAGE, message);

        LocalBroadcastManager.getInstance(app_context).sendBroadcast(it);
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
        private LocalBroadcastManager localBroadcastManager;
        private BroadcastReceiver storyDownloaded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(Download.DOWNLOAD_COMPLETE_EXTRA_ID, -1);
                Downloads d = new Downloads();
                if (d.hasID(id) && d.ownerOf(id).equals("ffnet")) {
                    Story s = Story.getStory(d.extraInfo(id));
                    Log.d(TAG, "onReceive: EXTRA:" + d.extraInfo(id));

                    s.markDownloaded(s.info.updated, intent.getStringExtra(Download.DOWNLOAD_COMPLETE_EXTRA_FILE), context);
                } else
                    Log.w(TAG, "onReceive: " + id + " DID NOT PASS");
            }
        };

        FFnet() {
            Archive.Pinned.bindDB(getDB());
            localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            localBroadcastManager.registerReceiver(storyDownloaded, new IntentFilter(Download.DOWNLOAD_COMPLETE));
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
            } else if (o.has("s_id")) {
                Story.getStory(o.optString("s_id")).onMessage(o);
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

        public Modules getModules() {
            return Modules.this;
        }
    }
}