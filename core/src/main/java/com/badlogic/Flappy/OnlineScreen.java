package com.badlogic.Flappy;

import com.badlogic.Flappy.net.NetClient;
import com.badlogic.Flappy.net.NetEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class OnlineScreen implements Screen {

    private final Main juego;

    private OrthographicCamera camara;
    private Viewport viewport;
    private SpriteBatch lote;
    private BitmapFont fuente;

    private NetClient red;

    // Estado de UI
    private String estado = "Buscando servidor...";
    private boolean listoYo = false;

    private boolean listoP1 = false;
    private boolean listoP2 = false;

    private boolean servidorEncontrado = false;
    private boolean conectado = false;

    public OnlineScreen(Main juego) {
        this.juego = juego;

        // Inicio networking desde el constructor (para que el lobby aparezca rápido)
        red = new NetClient(4321, 0);
        red.start();
        red.discover();
    }

    @Override
    public void show() {
        camara = new OrthographicCamera();
        viewport = new FitViewport(Constantes.VIRTUAL_WIDTH, Constantes.VIRTUAL_HEIGHT, camara);
        lote = new SpriteBatch();

        fuente = new BitmapFont();
        fuente.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {

        // 1) Consumir eventos de red
        leerRed();

        // 2) Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // volver al menú
            if (red != null) red.disconnect();
            juego.setScreen(new MenuScreen(juego));
            return;
        }

        // Toggle de listo con SPACE o click
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (conectado) {
                listoYo = !listoYo;
                red.setReady(listoYo);
            }
        }

        // 3) Dibujado
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        lote.setProjectionMatrix(camara.combined);
        lote.begin();

        fuente.draw(lote, "ONLINE (LAN)", 150, 520);
        fuente.draw(lote, "Estado: " + estado, 50, 470);

        fuente.draw(lote, "P1 READY: " + (listoP1 ? "SI" : "NO"), 50, 410);
        fuente.draw(lote, "P2 READY: " + (listoP2 ? "SI" : "NO"), 50, 370);

        if (!conectado) {
            fuente.draw(lote, "Esperando conexion...", 50, 310);
        } else {
            fuente.draw(lote, "SPACE/click: Ready " + (listoYo ? "(ON)" : "(OFF)"), 50, 310);
        }

        fuente.draw(lote, "ESC: volver", 50, 250);

        lote.end();
    }

    private void leerRed() {
        if (red == null) return;

        NetEvent evento;
        while ((evento = red.poll()) != null) {

            switch (evento.type) {

                case SERVER_FOUND:
                    servidorEncontrado = true;
                    estado = "Servidor encontrado (" + evento.raw + "). Conectando...";
                    // Auto-connect
                    red.connect();
                    break;

                case CONNECTED:
                    conectado = true;
                    estado = "Conectado. Elija READY.";
                    break;

                case LOBBY:
                    // LOBBY;READY_P1=0;READY_P2=1
                    parsearLobby(evento.raw);
                    break;

                case MATCH_STARTED:
                    estado = "Partida iniciada!";
                    // Cambio a PlayScreen en modo ONLINE
                    juego.setScreen(new PlayScreen(juego, red));
                    return;

                case SERVER_ERROR:
                    if (evento.detail != null) estado = "Error: " + evento.detail;
                    else estado = "Error: " + evento.raw;

                    // si el server resetea lobby, yo también apago mi ready local
                    listoYo = false;
                    break;

                case MATCH_ABORTED:
                    estado = "Partida abortada: " + evento.detail;
                    listoYo = false;
                    break;

                case INFO:
                    // logs opcionales
                    break;

                default:
                    break;
            }
        }

        if (!servidorEncontrado && "Buscando servidor...".equals(estado)) {
            // opcional: reintento de discovery
        }
    }

    private void parsearLobby(String raw) {
        // Esperado: LOBBY;READY_P1=0;READY_P2=1
        try {
            String[] partes = raw.split(";");
            for (String p : partes) {
                if (p.startsWith("READY_P1=")) listoP1 = p.endsWith("1");
                if (p.startsWith("READY_P2=")) listoP2 = p.endsWith("1");
            }
        } catch (Exception ignored) {}
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (red != null) red.disconnect();
        if (lote != null) lote.dispose();
        if (fuente != null) fuente.dispose();
    }
}
