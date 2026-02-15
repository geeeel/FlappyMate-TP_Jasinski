package com.badlogic.Flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Mate {
    private final Texture texture;
    private final Vector2 pos;
    private final Vector2 vel;
    private final Rectangle bounds;

    public Mate() {
        texture = new Texture("mate.png");
        pos = new Vector2(120, Constants.VIRTUAL_HEIGHT / 2f);
        vel = new Vector2(0, 0);
        bounds = new Rectangle(pos.x, pos.y, texture.getWidth(), texture.getHeight());
    }

    public void update(float dt) {
        vel.y += Constants.GRAVITY * dt;
        pos.y += vel.y * dt;
        bounds.setPosition(pos.x, pos.y);
    }

    public void jump() {
        vel.y = Constants.JUMP_VELOCITY;
    }

    public void reset() {
        pos.set(120, Constants.VIRTUAL_HEIGHT / 2f);
        vel.set(0, 0);
        bounds.setPosition(pos.x, pos.y);
    }

    public Texture getTexture() { return texture; }
    public Rectangle getBounds() { return bounds; }

    public float getX() { return pos.x; }
    public float getY() { return pos.y; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }

    public void dispose() {
        texture.dispose();
    }
}
