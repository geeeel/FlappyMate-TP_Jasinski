package com.badlogic.Flappy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class SimpleButton {

    private static Texture pixelBlanco; // textura blanca 1x1 generada

    private final Rectangle limites;
    private final String texto;

    public SimpleButton(float x, float y, float ancho, float alto, String texto) {
        this.limites = new Rectangle(x, y, ancho, alto);
        this.texto = texto;

        if (pixelBlanco == null) {
            // Genera una textura 1x1 blanca sin depender de archivos
            com.badlogic.gdx.graphics.Pixmap pm =
                new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

            pm.setColor(1, 1, 1, 1);
            pm.fill();

            pixelBlanco = new Texture(pm);
            pm.dispose();
        }
    }

    public void dibujar(SpriteBatch batch, BitmapFont fuente) {
        // Fondo del botón (gris oscuro semitransparente)
        batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        batch.draw(pixelBlanco, limites.x, limites.y, limites.width, limites.height);

        // Borde (blanco)
        batch.setColor(1, 1, 1, 0.9f);
        batch.draw(pixelBlanco, limites.x, limites.y, limites.width, 2);
        batch.draw(pixelBlanco, limites.x, limites.y + limites.height - 2, limites.width, 2);
        batch.draw(pixelBlanco, limites.x, limites.y, 2, limites.height);
        batch.draw(pixelBlanco, limites.x + limites.width - 2, limites.y, 2, limites.height);

        // Texto
        batch.setColor(1, 1, 1, 1);
        float textoX = limites.x + 20;
        float textoY = limites.y + limites.height * 0.65f;
        fuente.draw(batch, texto, textoX, textoY);
    }

    public boolean fueClickeadoDirecto() {
        if (!Gdx.input.justTouched()) return false;

        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY(); // invierte Y

        // Esto NO considera viewport/unproject: sirve solo si tu mundo coincide con pantalla.
        return limites.contains(x, y);
    }

    public boolean contiene(float x, float y) {
        return limites.contains(x, y);
    }

    public Rectangle obtenerLimites() {
        return limites;
    }

    public String obtenerTexto() {
        return texto;
    }
}
