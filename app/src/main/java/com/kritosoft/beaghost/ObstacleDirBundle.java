package com.kritosoft.beaghost;

/**
 * Created by Florian on 07.01.2016.
 */
public class ObstacleDirBundle {
    final float dir;
    final float pointX, pointY;
    final Obstacle o;
    final boolean onlyTouches;

    public ObstacleDirBundle(Obstacle o, float dir, float pointX, float pointY, boolean onlyTouches) {
        this.o = o;
        this.dir = dir;
        this.pointX = pointX;
        this.pointY = pointY;
        this.onlyTouches = onlyTouches;
    }


    public float getDir() {
        return dir;
    }

    public float getPointX() {
        return pointX;
    }

    public float getPointY() {
        return pointY;
    }

    public Obstacle getO() {
        return o;
    }
}
