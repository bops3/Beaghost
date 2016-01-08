package com.kritosoft.beaghost;

import android.graphics.Canvas;

public abstract class AbstractRobot implements Drawable {
    // STATIC ===========================================
    public final static float pi = (float) Math.PI, a = 1.051650213f;
    // ===================================================
    // GRÖSSEN +++++++++++++++++++++++
    public final float radius = 15f; // TODO GRÖSSE!
    public final float fov = (float) (0.25 * Math.PI);
    // Sichtfeld
    public final float viewfieldradius = 400f;
    // ++++++++++++++++++++++++++++++

    // BEWEGUNG ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected float wayChangePerSec = 100;
    protected float x, y;
    protected GameManager gm;
    private float dir, dirSin, dirCos;

    public AbstractRobot(float x, float y, float dir, GameManager gm) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        changeDir(0f);
        this.gm = gm;
    }

    public synchronized float getRadius() {
        return radius;
    }

    public synchronized float getX() {
        return x;
    }

    public synchronized float getY() {
        return y;
    }

    public synchronized float getDirCos() {
        return dirCos;
    }

    public synchronized float getDirSin() {
        return dirSin;
    }

    public synchronized float getDir() {
        return dir;
    }


    protected void changeDir(float change) {
        dir += change;
        dir %= 2 * pi;
        dirSin = (float) Math.sin(dir);
        dirCos = (float) Math.cos(dir);
    }


    /**
     * Must be SYNCHRONIZED with super.this!!!!!!!!
     *
     * @param delayMillis
     */
    public abstract void tick(long delayMillis);

    public abstract void draw(Canvas c);
}
