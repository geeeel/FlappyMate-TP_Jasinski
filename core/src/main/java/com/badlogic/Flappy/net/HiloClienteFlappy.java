package com.badlogic.Flappy.net;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public final class HiloClienteFlappy extends Thread {

    // =========================
    // Protocolo (igual al server)
    // =========================
    private static final String MSG_HANDSHAKE_OUT = "Hello_There";
    private static final String MSG_HANDSHAKE_IN  = "General_Kenobi";

    private static final String MSG_CONECTAR      = "Conectar";
    private static final String MSG_CONECTADO     = "Conectado";
    private static final String MSG_NO_REGISTRADO = "No_registrado";

    private static final String MSG_PING = "PING";
    private static final String MSG_PONG = "PONG";
    private static final String MSG_DISCONNECT = "DISCONNECT";

    private static final String MSG_PARTIDA_INICIADA = "PARTIDA_INICIADA";
    private static final String MSG_PARTIDA_ABORTADA = "PARTIDA_ABORTADA";

    private static final String PREFIX_READY = "READY="; // READY=1/0
    private static final String PREFIX_INPUT = "INPUT;"; // INPUT;jump=1;seq=123

    private static final String PREFIX_LOBBY = "LOBBY;";
    private static final String PREFIX_STATE = "STATE;";
    private static final String PREFIX_SERVER_ERROR = "SERVER_ERROR;";

    // =========================
    // Config
    // =========================
    private final int puertoServidor;
    private final int puertoLocal;

    private DatagramSocket socket;
    private volatile boolean activo = true;

    private volatile InetAddress serverIp = null;
    private volatile int serverPort = -1;

    private volatile boolean registrado = false;

    // keepalive: su server mata si pasan 5s sin mensajes, así que mando PING cada 1s al estar registrado
    private static final long KEEPALIVE_MS = 1000L;
    private volatile long lastSendMs = 0L;

    private final AtomicInteger seq = new AtomicInteger(0);

    private final NetInbox inbox;

    public HiloClienteFlappy(int puertoServidor, int puertoLocal, NetInbox inbox) {
        super("HiloClienteFlappy-UDP");
        this.puertoServidor = puertoServidor;
        this.puertoLocal = puertoLocal;
        this.inbox = inbox;
        initSocket();
    }

    private void initSocket() {
        try {
            socket = (puertoLocal <= 0) ? new DatagramSocket() : new DatagramSocket(puertoLocal);
            socket.setBroadcast(true);
            socket.setSoTimeout(50); // para no bloquear loop
            inbox.push(NetEvent.info("CLIENT", "Socket local en " + socket.getLocalPort()));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo abrir socket UDP cliente", e);
        }
    }

    // =========================
    // API (llamada desde su juego)
    // =========================
    public void discoverServer() {
        sendToBroadcast(MSG_HANDSHAKE_OUT, puertoServidor);
        inbox.push(NetEvent.info("DISCOVERY", "Broadcast " + MSG_HANDSHAKE_OUT + " -> puerto " + puertoServidor));
    }

    public void connect() {
        if (serverIp == null || serverPort <= 0) {
            inbox.push(NetEvent.info("CONNECT", "No puedo conectar: serverIp/serverPort null."));
            return;
        }
        send(MSG_CONECTAR);
    }

    public void setReady(boolean ready) {
        if (!registrado) return;
        send(PREFIX_READY + (ready ? "1" : "0"));
    }

    public void sendJump() {
        if (!registrado) return;
        int s = seq.incrementAndGet();
        send(PREFIX_INPUT + "jump=1;seq=" + s);
    }

    public void disconnect() {
        if (registrado) send(MSG_DISCONNECT);
        shutdown("client_disconnect");
    }

    public void shutdown(String reason) {
        inbox.push(NetEvent.info("CLIENT", "Shutdown reason=" + reason));
        activo = false;
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
    }

    public InetAddress getServerIp() { return serverIp; }
    public int getServerPort() { return serverPort; }
    public boolean isRegistrado() { return registrado; }

    // =========================
    // Loop
    // =========================
    @Override
    public void run() {
        while (activo) {

            // Keepalive
            long now = System.currentTimeMillis();
            if (registrado && (now - lastSendMs) >= KEEPALIVE_MS) {
                send(MSG_PING);
            }

            try {
                byte[] buf = new byte[1400];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                socket.receive(p);

                String msg = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8).trim();
                handleIncoming(p, msg);

            } catch (SocketTimeoutException ste) {
                // normal
            } catch (SocketException se) {
                if (!activo) break;
                inbox.push(NetEvent.info("SOCKET", "SocketException: " + se.getMessage()));
            } catch (Exception e) {
                inbox.push(NetEvent.info("CLIENT", "Excepción: " + e.getMessage()));
            }
        }
    }

    // =========================
    // Incoming
    // =========================
    private void handleIncoming(DatagramPacket p, String msg) {

        // Handshake
        if (MSG_HANDSHAKE_IN.equals(msg)) {
            serverIp = p.getAddress();
            serverPort = p.getPort();
            inbox.push(NetEvent.serverFound(serverIp.getHostAddress() + ":" + serverPort));
            inbox.push(NetEvent.info("DISCOVERY", "Server encontrado " + serverIp + ":" + serverPort));
            return;
        }

        // Inferencia del server si todavía no lo tengo (por si conectan manual o reciben directo)
        if (serverIp == null) {
            serverIp = p.getAddress();
            serverPort = p.getPort();
            inbox.push(NetEvent.serverFound(serverIp.getHostAddress() + ":" + serverPort));
            inbox.push(NetEvent.info("DISCOVERY", "Inferido server por paquete " + serverIp + ":" + serverPort));
        }

        // Registro
        if (MSG_CONECTADO.equals(msg)) {
            registrado = true;
            inbox.push(NetEvent.connected());
            inbox.push(NetEvent.info("CONNECT", "Conectado/registrado."));
            return;
        }

        if (MSG_NO_REGISTRADO.equals(msg)) {
            registrado = false;
            inbox.push(NetEvent.info("CONNECT", "Server dice No_registrado (perdí registro?)."));
            return;
        }

        // PONG
        if (MSG_PONG.equals(msg)) {
            return;
        }

        // Match
        if (MSG_PARTIDA_INICIADA.equals(msg)) {
            inbox.push(NetEvent.matchStarted());
            return;
        }

        if (MSG_PARTIDA_ABORTADA.equals(msg)) {
            inbox.push(NetEvent.matchAborted(msg));
            return;
        }

        // Error
        if (msg.startsWith(PREFIX_SERVER_ERROR)) {
            String detail = extractDetail(msg);
            inbox.push(NetEvent.serverError(msg, detail));
            return;
        }

        // Lobby
        if (msg.startsWith(PREFIX_LOBBY)) {
            inbox.push(NetEvent.lobby(msg));
            return;
        }

        // State
        if (msg.startsWith(PREFIX_STATE)) {
            StateSnapshot s = StateParser.parse(msg);
            if (s != null) inbox.push(NetEvent.state(msg, s));
            else inbox.push(NetEvent.info("PARSE", "No pude parsear STATE: " + msg));
            return;
        }

        // Otros
        inbox.push(NetEvent.info("MSG", msg));
    }

    private String extractDetail(String rawError) {
        int idx = rawError.indexOf("detail=");
        if (idx < 0) return null;
        return rawError.substring(idx + "detail=".length()).trim();
    }

    // =========================
    // UDP send
    // =========================
    private void send(String msg) {
        if (serverIp == null || serverPort <= 0) return;
        sendTo(serverIp, serverPort, msg);
    }

    private void sendTo(InetAddress ip, int port, String msg) {
        try {
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket p = new DatagramPacket(data, data.length, ip, port);
            socket.send(p);
            lastSendMs = System.currentTimeMillis();
        } catch (Exception e) {
            inbox.push(NetEvent.info("SEND", "Fallo a " + ip + ":" + port + " msg=" + msg));
        }
    }

    private void sendToBroadcast(String msg, int port) {
        try {
            InetAddress bcast = InetAddress.getByName("255.255.255.255");
            sendTo(bcast, port, msg);
        } catch (Exception e) {
            inbox.push(NetEvent.info("SEND", "Fallo broadcast msg=" + msg));
        }
    }
}
