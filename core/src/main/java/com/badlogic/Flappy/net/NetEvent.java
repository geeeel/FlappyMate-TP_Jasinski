package com.badlogic.Flappy.net;

public final class NetEvent {

    public enum Type {
        INFO,
        SERVER_FOUND,
        CONNECTED,
        LOBBY,
        MATCH_STARTED,
        MATCH_ABORTED,
        SERVER_ERROR,
        STATE
    }

    public final Type type;
    public final String raw;              // mensaje crudo si aplica
    public final StateSnapshot state;     // si es STATE
    public final String tag;              // para INFO
    public final String detail;           // para errores/abort

    private NetEvent(Type type, String raw, StateSnapshot state, String tag, String detail) {
        this.type = type;
        this.raw = raw;
        this.state = state;
        this.tag = tag;
        this.detail = detail;
    }

    public static NetEvent info(String tag, String msg) {
        return new NetEvent(Type.INFO, msg, null, tag, null);
    }

    public static NetEvent serverFound(String raw) {
        return new NetEvent(Type.SERVER_FOUND, raw, null, null, null);
    }

    public static NetEvent connected() {
        return new NetEvent(Type.CONNECTED, null, null, null, null);
    }

    public static NetEvent lobby(String raw) {
        return new NetEvent(Type.LOBBY, raw, null, null, null);
    }

    public static NetEvent matchStarted() {
        return new NetEvent(Type.MATCH_STARTED, null, null, null, null);
    }

    public static NetEvent matchAborted(String detailOrRaw) {
        return new NetEvent(Type.MATCH_ABORTED, detailOrRaw, null, null, detailOrRaw);
    }

    public static NetEvent serverError(String raw, String detail) {
        return new NetEvent(Type.SERVER_ERROR, raw, null, null, detail);
    }

    public static NetEvent state(String raw, StateSnapshot s) {
        return new NetEvent(Type.STATE, raw, s, null, null);
    }
}
