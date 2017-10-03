package com.github.shadowjonathan.automatiaapp.background;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class Operator extends Service {
    private static String TAG = "OPS";
    private final IBinder mBinder = new LocalBinder();
    private Comms C;
    private Modules M;

    public Comms getComms() {
        return C;
    }

    public Modules getModules() {
        return M;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "STARTED OPS SERVICE");
        super.onCreate();

        M = new Modules(this);
        C = new Comms(M, this);
        M.onComms(C);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //return super.onUnbind(intent);
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "OPS SERVICE DESTROYED");
        M.onDestroy();
        C.onDestroy();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public Operator getService() {
            return Operator.this;
        }
    }
}
