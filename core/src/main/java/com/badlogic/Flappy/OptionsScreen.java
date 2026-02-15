package com.badlogic.Flappy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class OptionsScreen implements Screen {

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Texture bg;
    private Texture ground;

    private BitmapFont titleFont;
    private BitmapFont font;

    private SimpleButton btnMusic;
    private SimpleButton btnSound;
    private SimpleButton btnBack;

    private float groundX1, groundX2;

    public OptionsScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        game.playMenuMusic();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();

        bg = new Texture("bg.png");
        ground = new Texture("ground.png");

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        font = new BitmapFont();
        font.getData().setScale(2f);

        float w = 320;
        float h = 70;
        float x = (Constants.VIRTUAL_WIDTH - w) / 2f;

        btnMusic = new SimpleButton(x, 480, w, h, "");
        btnSound = new SimpleButton(x, 390, w, h, "");
        btnBack  = new SimpleButton(x, 260, w, h, "Volver");

        groundX1 = 0;
        groundX2 = ground.getWidth();

        refreshTexts();
    }

    private void refreshTexts() {
        // Creamos nuevos botones con texto actualizado (simple y directo)
        // Para no rehacer la clase, recreamos botones manteniendo posiciones.
        btnMusic = new SimpleButton(btnMusic.getBounds().x, btnMusic.getBounds().y, btnMusic.getBounds().width, btnMusic.getBounds().height,
            "Musica: " + (Settings.musicEnabled ? "ON" : "OFF"));
        btnSound = new SimpleButton(btnSound.getBounds().x, btnSound.getBounds().y, btnSound.getBounds().width, btnSound.getBounds().height,
            "Sonido: " + (Settings.soundEnabled ? "ON" : "OFF"));
    }

    private boolean isClicked(SimpleButton b) {
        if (!Gdx.input.justTouched()) return false;
        Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(v);
        return b.contains(v.x, v.y);
    }

    private void update(float dt) {
        float dx = Constants.WORLD_SPEED * dt * 0.6f;
        groundX1 -= dx;
        groundX2 -= dx;
        if (groundX1 + ground.getWidth() < 0) groundX1 = groundX2 + ground.getWidth();
        if (groundX2 + ground.getWidth() < 0) groundX2 = groundX1 + ground.getWidth();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
        }

        if (isClicked(btnMusic)) {
            Settings.musicEnabled = !Settings.musicEnabled;

            // ✅ aplicar el cambio instantáneamente
            game.refreshMenuMusic();

            refreshTexts();
        } else if (isClicked(btnSound)) {
            Settings.soundEnabled = !Settings.soundEnabled;
            refreshTexts();
        } else if (isClicked(btnBack)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(bg, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        batch.draw(ground, groundX1, 0);
        batch.draw(ground, groundX2, 0);

        titleFont.draw(batch, "OPCIONES", 140, 720);

        btnMusic.draw(batch, font);
        btnSound.draw(batch, font);
        btnBack.draw(batch, font);

        // Controles
        font.draw(batch, "Controles:", 30, 200);
        font.draw(batch, "- SPACE / Click: Saltar", 30, 160);
        font.draw(batch, "- ESC: Volver", 30, 120);

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        bg.dispose();
        ground.dispose();
        titleFont.dispose();
        font.dispose();
    }
}
