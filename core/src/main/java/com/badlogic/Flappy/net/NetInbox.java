package com.badlogic.Flappy.net;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class NetInbox {

    private final ConcurrentLinkedQueue<NetEvent> q = new ConcurrentLinkedQueue<>();

    public void push(NetEvent e) {
        if (e != null) q.add(e);
    }

    public NetEvent poll() {
        return q.poll();
    }

    public void clear() {
        q.clear();
    }
}
