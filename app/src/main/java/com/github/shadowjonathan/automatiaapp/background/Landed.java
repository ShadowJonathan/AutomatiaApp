package com.github.shadowjonathan.automatiaapp.background;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.global.Downloads;
import com.github.shadowjonathan.automatiaapp.global.GlobalDBhelper;

public class Landed extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Log.i("DOWNLOAD", "onReceive: " + id);
        Downloads d = new Downloads(new GlobalDBhelper(context));
        if (d.hasID(id)) {
            d.reportDownloaded(id);
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            Intent i = new Intent(Download.DOWNLOAD_COMPLETE);
            i.putExtra(Download.DOWNLOAD_COMPLETE_EXTRA_ID, id);
            Log.d("LANDED", "onReceive: URI FOR DOWNLOAD: " + ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                    .getUriForDownloadedFile(id));
            i.putExtra(Download.DOWNLOAD_COMPLETE_EXTRA_FILE,
                    ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                            .getUriForDownloadedFile(id).toString());
            lbm.sendBroadcast(i);
        }
    }
}
