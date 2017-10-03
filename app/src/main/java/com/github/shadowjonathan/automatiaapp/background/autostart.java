package com.github.shadowjonathan.automatiaapp.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.R;

public class autostart extends BroadcastReceiver {
    private Context context;

    private SharedPreferences getPref() {
        return context.getSharedPreferences(
                context.getString(R.string.comms_pref_file_key), Context.MODE_PRIVATE);
    }

    public void onReceive(Context context, Intent i) {
        this.context = context;
        if (getPref().getBoolean(context.getString(R.string.boot_pref), false)) {
            Intent intent = new Intent(context, Operator.class);
            context.startService(intent);
            Log.i("Autostart", "started");
        } else
            Log.i("Autostart", "not started");
    }
}