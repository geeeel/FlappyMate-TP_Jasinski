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

public class OpcionesScreen implements Screen {

    private final Main juego;

    private OrthographicCamera camara;
    private Viewport viewport;
    private SpriteBatch lote;

    private Texture fondo;
    private Texture piso;

    private BitmapFont fuenteTitulo;
    private BitmapFont fuente;

    private SimpleButton btnMusica;
    private SimpleButton btnSonido;
    private SimpleButton btnVolver;

    private float pisoX1, pisoX2;

    public OpcionesScreen(Main juego) {
        this.juego = juego;
    }

    @Override
    public void show() {
        juego.reproducirMusicaMenu();

        camara = new OrthographicCamera();
        viewport = new FitViewport(Constantes.VIRTUAL_WIDTH, Constantes.VIRTUAL_HEIGHT, camara);
        lote = new SpriteBatch();

        fondo = new Texture("bg.png");
        piso = new Texture("ground.png");

        fuenteTitulo = new BitmapFont();
        fuenteTitulo.getData().setScale(3f);

        fuente = new BitmapFont();
        fuente.getData().setScale(2f);

        float ancho = 320;
        float alto = 70;
        float x = (Constantes.VIRTUAL_WIDTH - ancho) / 2f;

        btnMusica = new SimpleButton(x, 480, ancho, alto, "");
        btnSonido = new SimpleButton(x, 390, ancho, alto, "");
        btnVolver = new SimpleButton(x, 260, ancho, alto, "Volver");

        pisoX1 = 0;
        pisoX2 = piso.getWidth();

        refrescarTextos();
    }

    private void refrescarTextos() {
        // Recrea botones para actualizar el texto manteniendo posición y tamaño
        btnMusica = new SimpleButton(
            btnMusica.obtenerLimites().x,
            btnMusica.obtenerLimites().y,
            btnMusica.obtenerLimites().width,
            btnMusica.obtenerLimites().height,
            "Musica: " + (Config.musicEnabled ? "ON" : "OFF")
        );

        btnSonido = new SimpleButton(
            btnSonido.obtenerLimites().x,
            btnSonido.obtenerLimites().y,
            btnSonido.obtenerLimites().width,
            btnSonido.obtenerLimites().height,
            "Sonido: " + (Config.soundEnabled ? "ON" : "OFF")
        );
    }

    private boolean fueClickeado(SimpleButton boton) {
        if (!Gdx.input.justTouched()) return false;

        Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(v);

        return boton.contiene(v.x, v.y);
    }

    private void actualizar(float dt) {
        // Scroll del piso
        float dx = Constantes.VELOCIDAD_MUNDO * dt * 0.6f;
        pisoX1 -= dx;
        pisoX2 -= dx;

        if (pisoX1 + piso.getWidth() < 0) pisoX1 = pisoX2 + piso.getWidth();
        if (pisoX2 + piso.getWidth() < 0) pisoX2 = pisoX1 + piso.getWidth();

        // ESC vuelve al menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.setScreen(new MenuScreen(juego));
        }

        // Clicks
        if (fueClickeado(btnMusica)) {
            Config.musicEnabled = !Config.musicEnabled;

            // aplicar cambio instantáneo
            juego.actualizarMusicaMenu();

            refrescarTextos();

        } else if (fueClickeado(btnSonido)) {
            Config.soundEnabled = !Config.soundEnabled;
            refrescarTextos();

        } else if (fueClickeado(btnVolver)) {
            juego.setScreen(new MenuScreen(juego));
        }
    }

    @Override
    public void render(float delta) {
        actualizar(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        lote.setProjectionMatrix(camara.combined);
        lote.begin();

        lote.draw(fondo, 0, 0, Constantes.VIRTUAL_WIDTH, Constantes.VIRTUAL_HEIGHT);
        lote.draw(piso, pisoX1, 0);
        lote.draw(piso, pisoX2, 0);

        fuenteTitulo.draw(lote, "OPCIONES", 140, 720);

        btnMusica.dibujar(lote, fuente);
        btnSonido.dibujar(lote, fuente);
        btnVolver.dibujar(lote, fuente);

        // Texto de controles
        fuente.draw(lote, "Controles:", 30, 200);
        fuente.draw(lote, "- SPACE / Click: Saltar", 30, 160);
        fuente.draw(lote, "- ESC: Volver", 30, 120);

        lote.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        lote.dispose();
        fondo.dispose();
        piso.dispose();
        fuenteTitulo.dispose();
        fuente.dispose();
    }
}
