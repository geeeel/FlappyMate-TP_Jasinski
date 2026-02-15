package com.badlogic.Flappy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class SimpleButton {
    private static Texture whitePixel; // 1x1 blanco generado

    private final Rectangle bounds;
    private final String text;

    public SimpleButton(float x, float y, float w, float h, String text) {
        this.bounds = new Rectangle(x, y, w, h);
        this.text = text;

        if (whitePixel == null) {
            // Generamos textura blanca 1x1 sin assets externos
            com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pm.setColor(1,1,1,1);
            pm.fill();
            whitePixel = new Texture(pm);
            pm.dispose();
        }
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        // Fondo del botón (gris)
        batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        batch.draw(whitePixel, bounds.x, bounds.y, bounds.width, bounds.height);

        // Borde
        batch.setColor(1, 1, 1, 0.9f);
        batch.draw(whitePixel, bounds.x, bounds.y, bounds.width, 2);
        batch.draw(whitePixel, bounds.x, bounds.y + bounds.height - 2, bounds.width, 2);
        batch.draw(whitePixel, bounds.x, bounds.y, 2, bounds.height);
        batch.draw(whitePixel, bounds.x + bounds.width - 2, bounds.y, 2, bounds.height);

        // Texto
        batch.setColor(1, 1, 1, 1);
        float textX = bounds.x + 20;
        float textY = bounds.y + bounds.height * 0.65f;
        font.draw(batch, text, textX, textY);
    }

    public boolean justClicked() {
        if (!Gdx.input.justTouched()) return false;

        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY(); // invert Y (screen coords -> world-ish)

        // Como usamos FitViewport, mejor detectar con viewport luego.
        // En Menu/Options lo hacemos con viewport.unproject (ver abajo).
        // Acá queda por compatibilidad si lo quisieras usar “raw”.
        return bounds.contains(x, y);
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getText() {
        return text;
    }
}
