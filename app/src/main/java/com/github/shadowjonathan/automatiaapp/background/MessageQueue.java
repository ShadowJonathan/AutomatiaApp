package com.github.shadowjonathan.automatiaapp.background;


import java.util.Stack;

public abstract class MessageQueue {
    private Stack<Object> queue = new Stack<Object>();

    public abstract boolean condition();

    public abstract void send(Object o);

    public void in(Object o) {
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
}