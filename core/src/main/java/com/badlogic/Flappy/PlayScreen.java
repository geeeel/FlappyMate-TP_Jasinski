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

    private final Main game;

    // ONLINE opcional
    private final NetClient net;
    private final boolean online;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Texture bg;
    private Texture ground;
    private Texture termoTexture;

    private Sound sfxJump;
    private Sound sfxHit;
    private boolean playedHit = false;

    private Mate mate;                 // lo uso como "asset holder" (texture/size)
    private Array<TermoPair> termos;

    private float termoTimer = 0f;
    private int score = 0;
    private boolean gameOver = false;

    private BitmapFont font;

    private float groundX1 = 0;
    private float groundX2;

    // ONLINE cache
    private StateSnapshot lastState = null;

    // Dibujo P1 y P2 con un pequeño offset en X para distinguirlos
    private static final float P1_X = 120f;
    private static final float P2_X = 160f;

    // ====== OFFLINE ctor (igual que antes) ======
    public PlayScreen(Main game) {
        this.game = game;
        this.net = null;
        this.online = false;
    }

    // ====== ONLINE ctor (nuevo) ======
    public PlayScreen(Main game, NetClient net) {
        this.game = game;
        this.net = net;
        this.online = true;
    }

    @Override
    public void show() {
        game.stopMenuMusic();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();

        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        termoTexture = new Texture("termo.png");

        mate = new Mate();
        termos = new Array<>();

        sfxJump = Gdx.audio.newSound(Gdx.files.internal("mate_sound.mp3"));
        sfxHit  = Gdx.audio.newSound(Gdx.files.internal("bruh.mp3"));

        groundX1 = 0;
        groundX2 = ground.getWidth();

        font = new BitmapFont();
        font.getData().setScale(2f);

        if (online) {
            // En online no reseteo la partida local; espero STATE
            score = 0;
            gameOver = false;
        }
    }

    // ===== OFFLINE =====
    private void spawnTermos() {
        float y = MathUtils.random(Constants.TERMO_MIN_Y, Constants.TERMO_MAX_Y);
        termos.add(new TermoPair(termoTexture, Constants.VIRTUAL_WIDTH + 40, y));
    }

    private void setGameOver() {
        if (!gameOver) {
            gameOver = true;
            if (!playedHit && Settings.soundEnabled) {
                sfxHit.play(0.9f);
                playedHit = true;
            }
        }
    }

    private void resetGame() {
        playedHit = false;
        termos.clear();
        mate.reset();
        score = 0;
        termoTimer = 0f;
        gameOver = false;
        groundX1 = 0;
        groundX2 = ground.getWidth();
    }

    // ===== ONLINE: consumir red =====
    private void pollNetworkOnline() {
        if (net == null) return;

        NetEvent e;
        while ((e = net.poll()) != null) {
            switch (e.type) {

                case STATE:
                    lastState = e.state;
                    break;

                case SERVER_ERROR:
                    // si el server aborta/timeout/leave, corto y vuelvo al lobby online
                    game.setScreen(new OnlineScreen(game));
                    return;

                case MATCH_ABORTED:
                    game.setScreen(new OnlineScreen(game));
                    return;

                default:
                    break;
            }
        }
    }

    // ===== ONLINE: aplicar snapshot a objetos de render =====
    private void applyStateToRender(StateSnapshot s) {
        if (s == null) return;

        // Reconstruyo termos desde snapshot (simple y robusto)
        termos.clear();
        for (int i = 0; i < s.termoX.length; i++) {
            float x = s.termoX[i];
            float gapY = s.termoGap[i];
            termos.add(new TermoPair(termoTexture, x, gapY));
        }

        // Score: en su server hay score1/score2
        // Yo muestro score del "mejor" como fallback si usted no identifica jugador local.
        score = Math.max(s.p1score, s.p2score);

        // GameOver local: si ambos muertos (o ninguno state aún)
        boolean p1Alive = (s.p1alive == 1);
        boolean p2Alive = (s.p2alive == 1);
        gameOver = (!p1Alive && !p2Alive);
    }

    private void update(float dt) {

        // Volver al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (online && net != null) net.disconnect();
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (online) {
            // ONLINE: no simulo mundo. Solo input -> net.sendJump()
            pollNetworkOnline();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
                if (net != null) net.sendJump();
                if (Settings.soundEnabled) sfxJump.play(0.8f);
            }

            // Aplicar snapshot
            applyStateToRender(lastState);

            // Ground scroll visual local (no afecta la sim)
            float dx = Constants.WORLD_SPEED * dt;
            groundX1 -= dx;
            groundX2 -= dx;
            if (groundX1 + ground.getWidth() < 0) groundX1 = groundX2 + ground.getWidth();
            if (groundX2 + ground.getWidth() < 0) groundX2 = groundX1 + ground.getWidth();

            return;
        }

        // ===== OFFLINE (su lógica original) =====

        // Saltar / Reiniciar
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (gameOver) {
                resetGame();
            } else {
                mate.jump();
                if (Settings.soundEnabled) sfxJump.play(0.8f);
            }
        }

        // Si perdiste, no sigas actualizando el mundo
        if (gameOver) return;

        // Física del mate
        mate.update(dt);

        // Spawn termos
        termoTimer += dt;
        if (termoTimer >= Constants.TERMO_SPAWN_TIME) {
            termoTimer = 0f;
            spawnTermos();
        }

        // Update termos + score + remove
        for (int i = termos.size - 1; i >= 0; i--) {
            TermoPair t = termos.get(i);
            t.update(dt);

            if (t.shouldCountScore(mate.getX())) score++;
            if (t.isOffscreen()) termos.removeIndex(i);
        }

        // Ground scroll
        float dx = Constants.WORLD_SPEED * dt;
        groundX1 -= dx;
        groundX2 -= dx;
        if (groundX1 + ground.getWidth() < 0) groundX1 = groundX2 + ground.getWidth();
        if (groundX2 + ground.getWidth() < 0) groundX2 = groundX1 + ground.getWidth();

        // Colision: piso / techo
        if (mate.getY() <= Constants.GROUND_HEIGHT) setGameOver();
        if (mate.getY() + mate.getHeight() >= Constants.VIRTUAL_HEIGHT) setGameOver();

        // Collisions: termos
        for (TermoPair t : termos) {
            if (t.getBottomBounds().overlaps(mate.getBounds()) || t.getTopBounds().overlaps(mate.getBounds())) {
                setGameOver();
                break;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Fondo
        batch.draw(bg, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);

        font.draw(batch, "tick=" + net.lastTickApplied, 20, 240);
        font.draw(batch, "oldDrop=" + net.droppedOldStates, 20, 210);
        font.draw(batch, "missing=" + net.missingTicks, 20, 180);

        // Termos
        for (TermoPair t : termos) t.draw(batch);

        // Piso
        batch.draw(ground, groundX1, 0);
        batch.draw(ground, groundX2, 0);

        if (online) {
            // ONLINE: dibujo P1 y P2 en base a lastState (si todavía no llegó, dibujo centrado)
            float y1 = (lastState != null) ? lastState.p1y : (Constants.VIRTUAL_HEIGHT / 2f);
            float y2 = (lastState != null) ? lastState.p2y : (Constants.VIRTUAL_HEIGHT / 2f);

            // P1
            batch.setColor(1f, 1f, 1f, 1f); // blanco normal
            batch.draw(mate.getTexture(), P1_X, y1);

            // P2 (azulado por ejemplo)
            batch.setColor(0.6f, 0.8f, 1f, 1f);
            batch.draw(mate.getTexture(), P2_X, y2);

// volver a blanco para no teñir todo lo demás
            batch.setColor(1f, 1f, 1f, 1f);


            // HUD
            font.draw(batch, "ONLINE", 20, Constants.VIRTUAL_HEIGHT - 20);
            font.draw(batch, "Score: " + score, 20, Constants.VIRTUAL_HEIGHT - 60);

            if (lastState == null) {
                font.draw(batch, "Esperando STATE...", 120, 450);
            } else {
                if (lastState.p1alive == 0) font.draw(batch, "P1 DEAD", 20, 420);
                if (lastState.p2alive == 0) font.draw(batch, "P2 DEAD", 20, 380);
            }

            font.draw(batch, "SPACE/Tap: jump", 20, 320);
            font.draw(batch, "M: volver al menu", 20, 280);

            batch.end();
            return;
        }

        // OFFLINE: Mate
        batch.draw(mate.getTexture(), mate.getX(), mate.getY());

        // HUD
        font.draw(batch, "Score: " + score, 20, Constants.VIRTUAL_HEIGHT - 20);

        if (gameOver) {
            font.draw(batch, "GAME OVER", 150, 450);
            font.draw(batch, "SPACE / Tap: restart", 90, 400);
            font.draw(batch, "M: volver al menu", 120, 350);
        }

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (online && net != null) net.disconnect();

        if (batch != null) batch.dispose();
        if (sfxJump != null) sfxJump.dispose();
        if (sfxHit != null) sfxHit.dispose();
        if (bg != null) bg.dispose();
        if (ground != null) ground.dispose();
        if (termoTexture != null) termoTexture.dispose();
        if (mate != null) mate.dispose();
        if (font != null) font.dispose();
    }
}
