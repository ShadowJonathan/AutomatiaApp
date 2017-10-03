package com.github.shadowjonathan.automatiaapp.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.web.WebReader;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class Comms {
    private static final URI AUTOMATIA_WS;
    private static final URI AUTOMATIA_URL;
    private static final String WSTAG = "NETWORK/WS";

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

    private WebSocketClient ws;
    private Modules M;
    private ConnectivityManager cm;
    private Context app_context;
    private boolean online;
    private Timer connect_timer = new Timer();
    private TimerTask tt = new TimerTask() {
        @Override
        public void run() {
            if (online && (ws == null || ws.getReadyState() == WebSocket.READYSTATE.CLOSED)) {
                Log.i(WSTAG, "Found dead network link, trying to restart it...");
                newWS();
            } else if (ws != null && ws.getReadyState() != WebSocket.READYSTATE.OPEN) {
                Log.v(WSTAG, "Found a non-open network connection: " + ws.getReadyState());
            }
        }
    };

    private MessageQueue messages;

    public Comms(Modules m, Context context) {
        app_context = context;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        M = m;
        M.BindComms(this);
        messages = new MessageQueue() {
            @Override
            public boolean condition() {
                return ws != null;
            }

            @Override
            public void send(Object o) {
                ws.send(o.toString());
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
    }

    private void sendUUID(String uuid) {
        ws.send("{\"uuid\": \"" + uuid + "\"}");
        this.onReady();
    }

    // NETWORK
    private void onNetworkChange(boolean online) {
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
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(WSTAG, "CLOSE(" + (remote ? "server" : "local") + "): " + code + " > " + reason);
                ws = null;
                if (code != -1) ;
                // TODO
                // UIHandler.obtainMessage(MainActivity.CMD_TOAST, "Automatia Disconnected").sendToTarget();
            }

            @Override
            public void onError(Exception ex) {
                Log.e(WSTAG, "ERROR", ex);
            }

            @Override
            public void send(String text) {
                super.send(text);
                if (text.contains("\"pong\""))
                    Log.v(WSTAG, "OUT: " + text);
                else
                    Log.d(WSTAG, "OUT: " + text);
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
        // TODO
        // UIHandler.obtainMessage(MainActivity.CMD_TOAST, "Automatia Connected").sendToTarget();
    }

    public void onDestroy() {

    }
}
