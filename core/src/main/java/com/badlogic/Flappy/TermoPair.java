package com.badlogic.Flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class TermoPair {
    private final Texture termoTexture;
    private float x;
    private final float gapCenterY;

    private final Rectangle bottomBounds;
    private final Rectangle topBounds;

    private boolean countedScore = false;

    public TermoPair(Texture termoTexture, float startX, float gapCenterY) {
        this.termoTexture = termoTexture;
        this.x = startX;
        this.gapCenterY = gapCenterY;

        float termoW = termoTexture.getWidth();

        float bottomTopY = gapCenterY - Constants.TERMO_GAP / 2f;
        float topBottomY  = gapCenterY + Constants.TERMO_GAP / 2f;

        bottomBounds = new Rectangle(x, Constants.GROUND_HEIGHT, termoW, bottomTopY - Constants.GROUND_HEIGHT);
        topBounds = new Rectangle(x, topBottomY, termoW, Constants.VIRTUAL_HEIGHT - topBottomY);
    }

    public void update(float dt) {
        x -= Constants.WORLD_SPEED * dt;
        bottomBounds.x = x;
        topBounds.x = x;
    }

    public boolean isOffscreen() {
        return x + termoTexture.getWidth() < 0;
    }

    public boolean shouldCountScore(float mateX) {
        if (!countedScore && mateX > x + termoTexture.getWidth()) {
            countedScore = true;
            return true;
        }
        return false;
    }

    public Rectangle getBottomBounds() { return bottomBounds; }
    public Rectangle getTopBounds() { return topBounds; }

    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Termo de abajo
        batch.draw(termoTexture, bottomBounds.x, bottomBounds.y, bottomBounds.width, bottomBounds.height);

        // Termo de arriba invertido
        batch.draw(termoTexture, topBounds.x, topBounds.y + topBounds.height, topBounds.width, -topBounds.height);
    }
}
