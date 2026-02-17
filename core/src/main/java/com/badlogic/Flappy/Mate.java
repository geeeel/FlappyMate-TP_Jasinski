package com.badlogic.Flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Mate {

    private final Texture textura;
    private final Vector2 posicion;
    private final Vector2 velocidad;
    private final Rectangle limites;

    public Mate() {
        textura = new Texture("mate.png");
        posicion = new Vector2(120, Constantes.VIRTUAL_HEIGHT / 2f);
        velocidad = new Vector2(0, 0);
        limites = new Rectangle(posicion.x, posicion.y, textura.getWidth(), textura.getHeight());
    }

    public void actualizar(float deltaTiempo) {
        velocidad.y += Constantes.GRAVEDAD * deltaTiempo;
        posicion.y += velocidad.y * deltaTiempo;
        limites.setPosition(posicion.x, posicion.y);
    }

    public void saltar() {
        velocidad.y = Constantes.VELOCIDAD_SALTO;
    }

    public void reiniciar() {
        posicion.set(120, Constantes.VIRTUAL_HEIGHT / 2f);
        velocidad.set(0, 0);
        limites.setPosition(posicion.x, posicion.y);
    }

    public Texture obtenerTextura() { return textura; }
    public Rectangle obtenerLimites() { return limites; }

    public float obtenerX() { return posicion.x; }
    public float obtenerY() { return posicion.y; }
    public float obtenerAncho() { return limites.width; }
    public float obtenerAlto() { return limites.height; }

    public void destruir() {
        textura.dispose();
    }
}
