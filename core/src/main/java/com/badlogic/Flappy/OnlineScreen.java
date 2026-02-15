package com.badlogic.Flappy;

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

    public OnlineScreen(Main game) {
        this.game = game;
    }

    @Override public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.justTouched()) {
            game.setScreen(new MenuScreen(game));
        }

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "ONLINE (LAN)", 150, 520);
        font.draw(batch, "Proximamente...", 140, 480);
        font.draw(batch, "ESC o click para volver", 90, 420);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
