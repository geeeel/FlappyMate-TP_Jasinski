package com.badlogic.Flappy;

import com.badlogic.Flappy.net.NetClient;
import com.badlogic.Flappy.net.NetEvent;
import com.badlogic.Flappy.net.StateSnapshot;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen {

    private final Main juego;

    // ONLINE opcional
    private final NetClient red;
    private final boolean online;

    private OrthographicCamera camara;
    private Viewport viewport;
    private SpriteBatch lote;

    private Texture fondo;
    private Texture piso;
    private Texture texturaTermo;

    private Sound sfxSalto;
    private Sound sfxGolpe;
    private boolean golpeReproducido = false;

    private Mate mate;                 // lo usas como "asset holder" (textura/tamaño)
    private Array<Termo> termos;

    private float temporizadorTermo = 0f;
    private int puntaje = 0;
    private boolean gameOver = false;

    private BitmapFont fuente;

    private float pisoX1 = 0;
    private float pisoX2;

    // ONLINE cache
    private StateSnapshot ultimoEstado = null;

    // Dibujo P1 y P2 con un pequeño offset en X para distinguirlos
    private static final float P1_X = 120f;
    private static final float P2_X = 160f;

    // ===== OFFLINE ctor =====
    public PlayScreen(Main juego) {
        this.juego = juego;
        this.red = null;
        this.online = false;
    }

    // ===== ONLINE ctor =====
    public PlayScreen(Main juego, NetClient red) {
        this.juego = juego;
        this.red = red;
        this.online = true;
    }

    @Override
    public void show() {
        juego.detenerMusicaMenu();

        camara = new OrthographicCamera();
        viewport = new FitViewport(Constantes.VIRTUAL_WIDTH, Constantes.VIRTUAL_HEIGHT, camara);
        lote = new SpriteBatch();

        fondo = new Texture("bg.png");
        piso = new Texture("ground.png");
        texturaTermo = new Texture("termo.png");

        mate = new Mate();
        termos = new Array<>();

        sfxSalto = Gdx.audio.newSound(Gdx.files.internal("mate_sound.mp3"));
        sfxGolpe  = Gdx.audio.newSound(Gdx.files.internal("bruh.mp3"));

        pisoX1 = 0;
        pisoX2 = piso.getWidth();

        fuente = new BitmapFont();
        fuente.getData().setScale(2f);

        if (online) {
            // En online no reseteo la partida local; espero STATE
            puntaje = 0;
            gameOver = false;
        }
    }

    // ===== OFFLINE =====
    private void spawnearTermos() {
        float y = MathUtils.random(Constantes.TERMO_MIN_Y, Constantes.TERMO_MAX_Y);
        termos.add(new Termo(texturaTermo, Constantes.VIRTUAL_WIDTH + 40, y));
    }

    private void activarGameOver() {
        if (!gameOver) {
            gameOver = true;
            if (!golpeReproducido && Config.soundEnabled) {
                sfxGolpe.play(0.9f);
                golpeReproducido = true;
            }
        }
    }

    private void reiniciarJuego() {
        golpeReproducido = false;
        termos.clear();
        mate.reiniciar();
        puntaje = 0;
        temporizadorTermo = 0f;
        gameOver = false;
        pisoX1 = 0;
        pisoX2 = piso.getWidth();
    }

    // ===== ONLINE: consumir red =====
    private void leerRedOnline() {
        if (red == null) return;

        NetEvent e;
        while ((e = red.poll()) != null) {
            switch (e.type) {

                case STATE:
                    ultimoEstado = e.state;
                    break;

                case SERVER_ERROR:
                    juego.setScreen(new OnlineScreen(juego));
                    return;

                case MATCH_ABORTED:
                    juego.setScreen(new OnlineScreen(juego));
                    return;

                default:
                    break;
            }
        }
    }

    // ===== ONLINE: aplicar snapshot a objetos de render =====
    private void aplicarEstadoARender(StateSnapshot s) {
        if (s == null) return;

        // Reconstruyo termos desde snapshot
        termos.clear();
        for (int i = 0; i < s.termoX.length; i++) {
            float x = s.termoX[i];
            float gapY = s.termoGap[i];
            termos.add(new Termo(texturaTermo, x, gapY));
        }

        // Puntaje: muestro el mayor como fallback
        puntaje = Math.max(s.p1score, s.p2score);

        // GameOver si ambos muertos
        boolean p1Vivo = (s.p1alive == 1);
        boolean p2Vivo = (s.p2alive == 1);
        gameOver = (!p1Vivo && !p2Vivo);
    }

    private void actualizar(float dt) {

        // Volver al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (online && red != null) red.disconnect();
            juego.setScreen(new MenuScreen(juego));
            return;
        }

        if (online) {
            // ONLINE: no simulo mundo. Solo input -> red.sendJump()
            leerRedOnline();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
                if (red != null) red.sendJump();
                if (Config.soundEnabled) sfxSalto.play(0.8f);
            }

            // Aplicar snapshot a render
            aplicarEstadoARender(ultimoEstado);

            // Scroll visual local del piso
            float dx = Constantes.VELOCIDAD_MUNDO * dt;
            pisoX1 -= dx;
            pisoX2 -= dx;
            if (pisoX1 + piso.getWidth() < 0) pisoX1 = pisoX2 + piso.getWidth();
            if (pisoX2 + piso.getWidth() < 0) pisoX2 = pisoX1 + piso.getWidth();

            return;
        }

        // ===== OFFLINE =====

        // Saltar / Reiniciar
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (gameOver) {
                reiniciarJuego();
            } else {
                mate.saltar();
                if (Config.soundEnabled) sfxSalto.play(0.8f);
            }
        }

        // Si perdiste, no sigas actualizando el mundo
        if (gameOver) return;

        // Física del mate
        mate.actualizar(dt);

        // Spawn termos por timer
        temporizadorTermo += dt;
        if (temporizadorTermo >= Constantes.TIEMPO_SPAWN_TERMO) {
            temporizadorTermo = 0f;
            spawnearTermos();
        }

        // Update termos + score + remove
        for (int i = termos.size - 1; i >= 0; i--) {
            Termo t = termos.get(i);
            t.actualizar(dt);

            if (t.debeSumarPuntaje(mate.obtenerX())) puntaje++;
            if (t.estaFueraDePantalla()) termos.removeIndex(i);
        }

        // Scroll del piso
        float dx = Constantes.VELOCIDAD_MUNDO * dt;
        pisoX1 -= dx;
        pisoX2 -= dx;
        if (pisoX1 + piso.getWidth() < 0) pisoX1 = pisoX2 + piso.getWidth();
        if (pisoX2 + piso.getWidth() < 0) pisoX2 = pisoX1 + piso.getWidth();

        // Colisión: piso / techo
        if (mate.obtenerY() <= Constantes.ALTURA_PISO) activarGameOver();
        if (mate.obtenerY() + mate.obtenerAlto() >= Constantes.VIRTUAL_HEIGHT) activarGameOver();

        // Colisión: termos
        for (Termo t : termos) {
            if (t.obtenerLimitesInferior().overlaps(mate.obtenerLimites()) || t.obtenerLimitesSuperior().overlaps(mate.obtenerLimites())) {
                activarGameOver();
                break;
            }
        }
    }

    @Override
    public void render(float delta) {
        actualizar(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        lote.setProjectionMatrix(camara.combined);
        lote.begin();

        // Fondo
        lote.draw(fondo, 0, 0, Constantes.VIRTUAL_WIDTH, Constantes.VIRTUAL_HEIGHT);

        // Debug de red (ojo: esto rompe offline si red es null, te lo marco abajo)
        fuente.draw(lote, "tick=" + red.lastTickApplied, 20, 240);
        fuente.draw(lote, "oldDrop=" + red.droppedOldStates, 20, 210);
        fuente.draw(lote, "missing=" + red.missingTicks, 20, 180);

        // Termos
        for (Termo t : termos) t.dibujar(lote);

        // Piso
        lote.draw(piso, pisoX1, 0);
        lote.draw(piso, pisoX2, 0);

        if (online) {
            float y1 = (ultimoEstado != null) ? ultimoEstado.p1y : (Constantes.VIRTUAL_HEIGHT / 2f);
            float y2 = (ultimoEstado != null) ? ultimoEstado.p2y : (Constantes.VIRTUAL_HEIGHT / 2f);

            // P1 (blanco)
            lote.setColor(1f, 1f, 1f, 1f);
            lote.draw(mate.obtenerTextura(), P1_X, y1);

            // P2 (tinte azulado)
            lote.setColor(0.6f, 0.8f, 1f, 1f);
            lote.draw(mate.obtenerTextura(), P2_X, y2);

            // volver a blanco
            lote.setColor(1f, 1f, 1f, 1f);

            // HUD
            fuente.draw(lote, "ONLINE", 20, Constantes.VIRTUAL_HEIGHT - 20);
            fuente.draw(lote, "Score: " + puntaje, 20, Constantes.VIRTUAL_HEIGHT - 60);

            if (ultimoEstado == null) {
                fuente.draw(lote, "Esperando STATE...", 120, 450);
            } else {
                if (ultimoEstado.p1alive == 0) fuente.draw(lote, "P1 DEAD", 20, 420);
                if (ultimoEstado.p2alive == 0) fuente.draw(lote, "P2 DEAD", 20, 380);
            }

            fuente.draw(lote, "SPACE/Tap: jump", 20, 320);
            fuente.draw(lote, "M: volver al menu", 20, 280);

            lote.end();
            return;
        }

        // OFFLINE: Mate
        lote.draw(mate.obtenerTextura(), mate.obtenerX(), mate.obtenerY());

        // HUD
        fuente.draw(lote, "Score: " + puntaje, 20, Constantes.VIRTUAL_HEIGHT - 20);

        if (gameOver) {
            fuente.draw(lote, "GAME OVER", 150, 450);
            fuente.draw(lote, "SPACE / Tap: restart", 90, 400);
            fuente.draw(lote, "M: volver al menu", 120, 350);
        }

        lote.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (online && red != null) red.disconnect();

        if (lote != null) lote.dispose();
        if (sfxSalto != null) sfxSalto.dispose();
        if (sfxGolpe != null) sfxGolpe.dispose();
        if (fondo != null) fondo.dispose();
        if (piso != null) piso.dispose();
        if (texturaTermo != null) texturaTermo.dispose();
        if (mate != null) mate.destruir();
        if (fuente != null) fuente.dispose();
    }
}
