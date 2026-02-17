package com.badlogic.Flappy;

public class Constantes {

    // Tamaño del mundo virtual (no depende de la resolución real)
    public static final int VIRTUAL_WIDTH = 480;
    public static final int VIRTUAL_HEIGHT = 800;

    // Física del mate
    public static final float GRAVEDAD = -1200f;
    public static final float VELOCIDAD_SALTO = 420f;

    // Velocidad horizontal del mundo (termos y piso)
    public static final float VELOCIDAD_MUNDO = 200f;

    // Hueco entre los termos
    public static final float TERMO_GAP = 170f;

    // Cada cuánto aparece un nuevo termo
    public static final float TIEMPO_SPAWN_TERMO = 1.2f;

    // Rango vertical donde puede aparecer el centro del hueco
    public static final float TERMO_MIN_Y = 160f;
    public static final float TERMO_MAX_Y = 620f;

    // Altura del piso
    public static final float ALTURA_PISO = 110f;
}
