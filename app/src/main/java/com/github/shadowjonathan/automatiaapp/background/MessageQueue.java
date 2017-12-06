package com.github.shadowjonathan.automatiaapp.background;


import android.content.Context;
import android.util.Log;

import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Stack;

public abstract class MessageQueue {
    private static final String TAG = "STACK_DEBUG";
    @SuppressWarnings("unchecked")
    private ToughStack<String> queue;

    MessageQueue(Context context) {
        queue = new ToughStack<>(context);
        queue.restore();
    }

    public abstract boolean condition();

    public abstract void send(String o);

    public void in(String o) {
        if (condition()) {
            if (!queue.isEmpty()) {
                flush();
            }
            send(o);
        } else {
            queue.push(o);
        }
    }

    public void flush() {
        while (!queue.isEmpty()) {
            send(queue.pop());
        }
    }

    public void check() {
        if (condition())
            if (!queue.isEmpty())
                flush();
    }

    private static class ToughStack<T> extends Stack<T> {
        private Context context;

        ToughStack(Context context) {
            super();
            this.context = context;
        }

        public void restore() {
            String data = Helper.readMessageQueue(context);
            if (!data.isEmpty()) {
                for (Object s : new Gson().fromJson(data, ArrayList.class)) {
                    this.push((T) s);
                }
                Log.d(TAG, "restore: " + data);
            }
        }

        @Override
        public T push(T item) {
            T r = super.push(item);
            sync();
            return r;
        }

        @Override
        public synchronized T pop() {
            T r = super.pop();
            sync();
            return r;
        }

        private void sync() {
            String s = new Gson().toJson(this);
            Helper.writeMessageQueue(s, context);
            Log.v(TAG, "sync: " + s);
        }
    }
}