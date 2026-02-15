package com.badlogic.Flappy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Main extends Game {

    private Music menuMusic;

    @Override
    public void create() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("jurabastu.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);

        playMenuMusic();
        setScreen(new MenuScreen(this));
    }

    public void playMenuMusic() {
        if (!Settings.musicEnabled) return;
        if (menuMusic != null && !menuMusic.isPlaying()) menuMusic.play();
    }

    public void pauseMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) menuMusic.pause();
    }

    public void stopMenuMusic() {
        if (menuMusic != null) menuMusic.stop();
    }

    public void refreshMenuMusic() {
        if (Settings.musicEnabled) playMenuMusic();
        else {
            pauseMenuMusic();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (menuMusic != null) menuMusic.dispose();
    }
}
