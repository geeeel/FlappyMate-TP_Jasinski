package com.badlogic.Flappy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Main extends Game {

    private Music musicaMenu;

    @Override
    public void create() {

        // Cargar música
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("jurabastu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(0.5f);

        reproducirMusicaMenu();

        // Pantalla inicial
        setScreen(new MenuScreen(this));
    }

    public void reproducirMusicaMenu() {
        if (!Config.musicEnabled) return;

        if (musicaMenu != null && !musicaMenu.isPlaying())
            musicaMenu.play();
    }

    public void pausarMusicaMenu() {
        if (musicaMenu != null && musicaMenu.isPlaying())
            musicaMenu.pause();
    }

    public void detenerMusicaMenu() {
        if (musicaMenu != null)
            musicaMenu.stop();
    }

    public void actualizarMusicaMenu() {
        if (Config.musicEnabled)
            reproducirMusicaMenu();
        else
            pausarMusicaMenu();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (musicaMenu != null)
            musicaMenu.dispose();
    }
}
