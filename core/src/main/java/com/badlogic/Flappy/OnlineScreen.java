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

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;

    private NetClient net;

    // UI state
    private String status = "Buscando servidor...";
    private boolean readyMe = false;

    private boolean readyP1 = false;
    private boolean readyP2 = false;

    private boolean serverFound = false;
    private boolean connected = false;

    public OnlineScreen(Main game) {
        this.game = game;

        // Arranco networking ya desde el constructor (así el lobby aparece rápido)
        net = new NetClient(4321, 0);
        net.start();
        net.discover();
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {

        // 1) Consumir eventos de red
        pollNetwork();

        // 2) Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // volver al menú
            if (net != null) net.disconnect();
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Toggle ready con SPACE o click
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (connected) {
                readyMe = !readyMe;
                net.setReady(readyMe);
            }
        }

        // Redibujar
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch, "ONLINE (LAN)", 150, 520);

        font.draw(batch, "Estado: " + status, 50, 470);

        font.draw(batch, "P1 READY: " + (readyP1 ? "SI" : "NO"), 50, 410);
        font.draw(batch, "P2 READY: " + (readyP2 ? "SI" : "NO"), 50, 370);

        if (!connected) {
            font.draw(batch, "Esperando conexion...", 50, 310);
        } else {
            font.draw(batch, "SPACE/click: Ready " + (readyMe ? "(ON)" : "(OFF)"), 50, 310);
        }

        font.draw(batch, "ESC: volver", 50, 250);

        batch.end();
    }

    private void pollNetwork() {
        if (net == null) return;

        NetEvent e;
        while ((e = net.poll()) != null) {
            switch (e.type) {

                case SERVER_FOUND:
                    serverFound = true;
                    status = "Servidor encontrado (" + e.raw + "). Conectando...";
                    // Auto-connect: para que funcione sin que usted haga nada
                    net.connect();
                    break;

                case CONNECTED:
                    connected = true;
                    status = "Conectado. Elija READY.";
                    break;

                case LOBBY:
                    // LOBBY;READY_P1=0;READY_P2=1
                    parseLobby(e.raw);
                    break;

                case MATCH_STARTED:
                    status = "Partida iniciada!";
                    // Cambio a PlayScreen en modo ONLINE
                    game.setScreen(new PlayScreen(game, net));
                    return;

                case SERVER_ERROR:
                    // SERVER_ERROR;code=...;detail=...
                    if (e.detail != null) status = "Error: " + e.detail;
                    else status = "Error: " + e.raw;
                    // si fue abort, su server resetea lobby; yo también apago mi ready local
                    readyMe = false;
                    break;

                case MATCH_ABORTED:
                    status = "Partida abortada: " + e.detail;
                    readyMe = false;
                    break;

                case INFO:
                    // si quiere logs, cámbielo a System.out.println
                    break;

                default:
                    break;
            }
        }

        if (!serverFound && "Buscando servidor...".equals(status)) {
            // Opcional: reintento de discovery (no spameo, solo si quiere)
        }
    }

    private void parseLobby(String raw) {
        // Esperado: LOBBY;READY_P1=0;READY_P2=1
        try {
            String[] parts = raw.split(";");
            for (String p : parts) {
                if (p.startsWith("READY_P1=")) readyP1 = p.endsWith("1");
                if (p.startsWith("READY_P2=")) readyP2 = p.endsWith("1");
            }
        } catch (Exception ignored) {}
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (net != null) net.disconnect();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
