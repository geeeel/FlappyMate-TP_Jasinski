package com.badlogic.Flappy.net;

public final class StateSnapshot {
    public int tick;

    public float p1y, p1vy;
    public int p1alive; // 1/0
    public int p1score;

    public float p2y, p2vy;
    public int p2alive; // 1/0
    public int p2score;

    public float[] termoX = new float[0];
    public float[] termoGap = new float[0];
}
