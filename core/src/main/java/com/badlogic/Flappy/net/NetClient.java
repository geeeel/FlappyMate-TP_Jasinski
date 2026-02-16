package com.badlogic.Flappy.net;

public final class NetClient {

    private final NetInbox inbox = new NetInbox();
    private final HiloClienteFlappy hilo;

    // Estos dos los dejo por comodidad para el juego:
    public volatile StateSnapshot lastState = null;
    public volatile String lastLobbyRaw = null;

    public volatile boolean matchStarted = false;
    public volatile boolean connected = false;

    // en NetClient
    public volatile int lastTickApplied = -1;
    public volatile int droppedOldStates = 0;
    public volatile int missingTicks = 0;

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
                if (e.state != null) {
                    int t = e.state.tick;

                    if (lastTickApplied != -1 && t <= lastTickApplied) {
                        droppedOldStates++;
                        // DESCARTO por fuera de orden o repetido
                        break;
                    }

                    if (lastTickApplied != -1 && t > lastTickApplied + 1) {
                        missingTicks += (t - (lastTickApplied + 1));
                    }

                    lastTickApplied = t;
                    lastState = e.state;
                }
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
