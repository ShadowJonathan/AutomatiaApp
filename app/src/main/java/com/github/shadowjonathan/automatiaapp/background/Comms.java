package com.github.shadowjonathan.automatiaapp.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.web.WebReader;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Comms {
    private static final URI AUTOMATIA_WS;
    private static final URI AUTOMATIA_URL;
    private static final String WSTAG = "NETWORK/WS";
    private static final int online_n_id = 9001;

    static {
        URI tmp = null;
        try {
            tmp = new URI("ws://automatia.tk/ws");
        } catch (URISyntaxException e) {
            // Handle exception.
        }
        AUTOMATIA_WS = tmp;

        tmp = null;
        try {
            tmp = new URI("http://automatia.tk");
        } catch (URISyntaxException e) {
            // Handle exception.
        }
        AUTOMATIA_URL = tmp;
    }

    private Date latestOnline;
    private Date latestOffline;
    private WebSocketClient ws;
    private Modules M;
    private ConnectivityManager cm;
    private Context app_context;
    private boolean online;
    private Timer connect_timer = new Timer();
    private MessageQueue messages;
    private TimerTask tt = new TimerTask() {
        @Override
        public void run() {
            updateNotification();
            if (online && (ws == null || ws.getReadyState() == WebSocket.READYSTATE.CLOSED)) {
                Log.i(WSTAG, "Found dead network link, trying to restart it...");
                newWS();
            } else if (ws != null && ws.getReadyState() != WebSocket.READYSTATE.OPEN) {
                Log.v(WSTAG, "Found a non-open network connection: " + ws.getReadyState());
            }
            messages.check();
        }
    };

    public Comms(Modules m, Context context) {
        app_context = context;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        M = m;
        M.BindComms(this);
        messages = new MessageQueue(context) {
            @Override
            public boolean condition() {
                return ws != null && ws.getReadyState() == WebSocket.READYSTATE.OPEN;
            }

            @Override
            public void send(String o) {
                ws.send(o);
            }
        };


        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String log = "Online: " + (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) ? "No" : "Yes") + "\nRaw: " + intent.getExtras() + "\n" + (cm.getActiveNetworkInfo() == null ? 0 : cm.getActiveNetworkInfo().getType());
                Log.d("NETWORK", log);
                //Toast.makeText(context, log, Toast.LENGTH_LONG).show();

                onNetworkChange((cm.getActiveNetworkInfo() == null ? 0 : cm.getActiveNetworkInfo().getType()) == 1);
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(br, filter);

        connect_timer.scheduleAtFixedRate(tt, 0, 5000);
        updateNotification();
    }

    private SharedPreferences getPref() {
        return app_context.getSharedPreferences(
                app_context.getString(R.string.comms_pref_file_key), Context.MODE_PRIVATE);
    }

    // UUID
    private String getUUID() {
        return getPref().getString(app_context.getString(R.string.comms_pref_UUID), "");
    }

    private void setUUID(String s) {
        getPref().edit().putString(app_context.getString(R.string.comms_pref_UUID), s).apply();
        updateNotification();
    }

    private void sendUUID(String uuid) {
        ws.send("{\"uuid\": \"" + uuid + "\"}");
        this.onReady();
    }

    private Notification.Builder makeBaseNotification() {
        return Helper.Notification.perm(app_context)
                .setSubText("Communication")
                ;
    }

    private void updateNotification() {
        if (getPref().getBoolean(app_context.getString(R.string.comms_pref_notification), true))
            ((NotificationManager) app_context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(online_n_id,
                            makeBaseNotification()
                                    .setSmallIcon(this.isOnline() ? R.drawable.ic_wifi : R.drawable.ic_wifi_off)
                                    .setContentTitle(this.isOnline() ? "Online" : "Offline")
                                    .setContentText(
                                            this.online ?
                                                    "For " + Helper.TSUtils.millisToLongDHMS(new Date().getTime() - latestOffline.getTime()) :
                                                    // "(UUID: " + getUUID() + ")"
                                                    latestOnline == null ?
                                                            null :
                                                            "For " + Helper.TSUtils.millisToLongDHMS(new Date().getTime() - latestOnline.getTime()) +
                                                                    (DateUtils.isToday(new Date().getTime()) ?
                                                                            (" (since " + new SimpleDateFormat("HH:mm").format(latestOnline) + ")") :
                                                                            (" (since " + new SimpleDateFormat("MM/dd HH:mm").format(latestOnline) + ")")
                                                                    )
                                    )
                                    .build()
                    );
        Log.v("COMMS_NOTIFICATION", (this.isOnline() ? "Online" : "Offline") + " | "
                + (this.isOnline() ?
                "For " + Helper.TSUtils.millisToLongDHMS(new Date().getTime() - latestOffline.getTime()) : // "(UUID: " + getUUID() + ")"
                latestOnline == null ?
                        null :
                        "For " + Helper.TSUtils.millisToLongDHMS(new Date().getTime() - latestOnline.getTime()) +
                                (DateUtils.isToday(new Date().getTime()) ?
                                        (" (since " + new SimpleDateFormat("HH:mm").format(latestOnline) + ")") :
                                        (" (since " + new SimpleDateFormat("MM/dd HH:mm").format(latestOnline) + ")")
                                )));
    }

    // NETWORK
    private void onNetworkChange(boolean online) {
        if (this.online != online) {
            if (!this.online)
                latestOffline = new Date();
        }
        this.online = online;
        if (!online) {
            if (ws != null)
                ws.close();
            ws = null;
        } else {
            if (ws == null || ws.getReadyState() != WebSocket.READYSTATE.OPEN) {
                newWS();
            }
        }
        updateNotification();
    }

    public boolean isOnline() {
        return this.online && ws != null && ws.getReadyState() == WebSocket.READYSTATE.OPEN;
    }

    private WebSocketClient newWS() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
        ws = new WebSocketClient(AUTOMATIA_WS) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d(WSTAG, "OPEN: " + handshakedata);
            }

            @Override
            public void onMessage(String message) {
                if (message.contains("\"ping\""))
                    Log.v(WSTAG, "IN: " + message);
                else
                    Log.d(WSTAG, "IN: " + message);
                Comms.this.onMessage(message);
                latestOnline = new Date();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(WSTAG, "CLOSE(" + (remote ? "server" : "local") + "): " + code + " > " + reason);
                ws = null;
                latestOffline = new Date();
            }

            @Override
            public void onError(Exception ex) {
                Log.e(WSTAG, "ERROR", ex);
            }

            @Override
            public void send(String text) {
                if (text.contains("\"pong\""))
                    Log.v(WSTAG, "OUT: " + text);
                else
                    Log.d(WSTAG, "OUT: " + text);
                super.send(text);
            }
        };
        ws.connect();
        return ws;
    }

    private void onMessage(String text) {
        JSONObject object;
        try {
            object = new JSONObject(text);

            if (object.has("ping") && object.getBoolean("ping")) {
                if (ws != null)
                    ws.send("{\"pong\":true}");
            } else if (object.has("login") && object.getBoolean("login")) {
                String uuid = getUUID();
                if (uuid.isEmpty())
                    new WebReader(new WebReader.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            setUUID(output);
                            sendUUID(output);
                        }
                    }).execute(AUTOMATIA_URL.resolve("/register").toString());
                else
                    sendUUID(uuid);
            } else {
                M.onMessage(object);
            }
        } catch (JSONException ex) {
            Log.e(WSTAG, "JSON_ERR: \"" + text + "\"\nERR: " + ex);
        }
    }

    public void send(JSONObject o) {
        this.send(o.toString());
    }

    public void send(String s) {
        messages.in(s);
    }

    // ON
    private void onReady() {
        M.onReady();
        messages.flush();
    }

    public void onDestroy() {

    }
}
