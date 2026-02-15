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

public class MenuScreen implements Screen {

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Texture bg;
    private Texture ground;

    private BitmapFont titleFont;
    private BitmapFont font;

    private SimpleButton btnJugar;
    private SimpleButton btnOnline;
    private SimpleButton btnOpciones;
    private SimpleButton btnSalir;

    private float groundX1, groundX2;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        ((Main)game).playMenuMusic();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();

        bg = new Texture("bg.png");
        ground = new Texture("ground.png");

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        font = new BitmapFont();
        font.getData().setScale(2f);

        float w = 280;
        float h = 70;
        float x = (Constants.VIRTUAL_WIDTH - w) / 2f;

        float startY = 460;
        float gap = 85;

        btnJugar = new SimpleButton(x, startY, w, h, "Jugar");
        btnOnline = new SimpleButton(x, startY - gap, w, h, "Online");
        btnOpciones = new SimpleButton(x, startY - gap * 2, w, h, "Opciones");
        btnSalir = new SimpleButton(x, startY - gap * 3, w, h, "Salir");

        groundX1 = 0;
        groundX2 = ground.getWidth();
    }

    private boolean isClicked(SimpleButton b) {
        if (!Gdx.input.justTouched()) return false;

        Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(v);

        return b.contains(v.x, v.y);
    }

    private void update(float dt) {
        // Scroll del piso (opcional para que el menú tenga vida)
        float dx = Constants.WORLD_SPEED * dt * 0.6f;
        groundX1 -= dx;
        groundX2 -= dx;
        if (groundX1 + ground.getWidth() < 0) groundX1 = groundX2 + ground.getWidth();
        if (groundX2 + ground.getWidth() < 0) groundX2 = groundX1 + ground.getWidth();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (isClicked(btnJugar)) {
            game.setScreen(new PlayScreen(game));
        } else if (isClicked(btnOnline)) {
            // Por ahora: placeholder
            game.setScreen(new OnlineScreen(game));
        } else if (isClicked(btnOpciones)) {
            game.setScreen(new OptionsScreen(game));
        } else if (isClicked(btnSalir)) {
            Gdx.app.exit();
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

        titleFont.draw(batch, "MATE FLAPPY", 120, 720);

        btnJugar.draw(batch, font);
        btnOnline.draw(batch, font);
        btnOpciones.draw(batch, font);
        btnSalir.draw(batch, font);

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
