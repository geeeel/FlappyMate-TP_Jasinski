package com.badlogic.Flappy.net;

public final class NetClient {

    private final NetInbox inbox = new NetInbox();
    private final HiloClienteFlappy hilo;

    // Estos dos los dejo por comodidad para el juego:
    public volatile StateSnapshot lastState = null;
    public volatile String lastLobbyRaw = null;

    public volatile boolean matchStarted = false;
    public volatile boolean connected = false;

    public NetClient(int puertoServidor, int puertoLocal) {
        this.hilo = new HiloClienteFlappy(puertoServidor, puertoLocal, inbox);
    }

    public void start() {
        hilo.start();
    }

    public void discover() {
        hilo.discoverServer();
    }

    public void connect() {
        hilo.connect();
    }

    public void setReady(boolean ready) {
        hilo.setReady(ready);
    }

    public void sendJump() {
        hilo.sendJump();
    }

    public void disconnect() {
        hilo.disconnect();
    }

    public void shutdown(String reason) {
        hilo.shutdown(reason);
    }

    public NetEvent poll() {
        NetEvent e = inbox.poll();
        if (e == null) return null;

        // Actualizo “cache” simple
        switch (e.type) {
            case CONNECTED:
                connected = true;
                break;
            case LOBBY:
                lastLobbyRaw = e.raw;
                break;
            case MATCH_STARTED:
                matchStarted = true;
                break;
            case MATCH_ABORTED:
                matchStarted = false;
                break;
            case STATE:
                lastState = e.state;
                break;
            default:
                break;
        }

        return e;
    }

    public boolean isRegistrado() {
        return hilo.isRegistrado();
    }
}
