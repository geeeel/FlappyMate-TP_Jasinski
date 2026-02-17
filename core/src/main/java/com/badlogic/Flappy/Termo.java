package com.badlogic.Flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Termo {

    private final Texture texturaTermo;

    private float x;
    private final float centroGapY;

    private final Rectangle limitesInferior;
    private final Rectangle limitesSuperior;

    private boolean puntajeContado = false;

    public Termo(Texture texturaTermo, float xInicial, float centroGapY) {
        this.texturaTermo = texturaTermo;
        this.x = xInicial;
        this.centroGapY = centroGapY;

        float anchoTermo = texturaTermo.getWidth();

        float parteSuperiorInferiorY = centroGapY - Constantes.TERMO_GAP / 2f;
        float parteInferiorSuperiorY = centroGapY + Constantes.TERMO_GAP / 2f;

        limitesInferior = new Rectangle(
            x,
            Constantes.ALTURA_PISO,
            anchoTermo,
            parteSuperiorInferiorY - Constantes.ALTURA_PISO
        );

        limitesSuperior = new Rectangle(
            x,
            parteInferiorSuperiorY,
            anchoTermo,
            Constantes.VIRTUAL_HEIGHT - parteInferiorSuperiorY
        );
    }

    public void actualizar(float dt) {
        x -= Constantes.VELOCIDAD_MUNDO * dt;
        limitesInferior.x = x;
        limitesSuperior.x = x;
    }

    public boolean estaFueraDePantalla() {
        return x + texturaTermo.getWidth() < 0;
    }

    public boolean debeSumarPuntaje(float mateX) {
        if (!puntajeContado && mateX > x + texturaTermo.getWidth()) {
            puntajeContado = true;
            return true;
        }
        return false;
    }

    public Rectangle obtenerLimitesInferior() { return limitesInferior; }
    public Rectangle obtenerLimitesSuperior() { return limitesSuperior; }

    public void dibujar(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {

        // Termo de abajo
        batch.draw(texturaTermo,
            limitesInferior.x,
            limitesInferior.y,
            limitesInferior.width,
            limitesInferior.height);

        // Termo de arriba (invertido verticalmente)
        batch.draw(texturaTermo,
            limitesSuperior.x,
            limitesSuperior.y + limitesSuperior.height,
            limitesSuperior.width,
            -limitesSuperior.height);
    }
}
