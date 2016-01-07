package com.kritosoft.beaghost;

import java.util.ArrayList;

/**
 * Created by Florian on 07.01.2016.
 */
public class ObstacleDirBundle {
    final float dir;
    final float pointX, pointY;
    final private Obstacle o;

    public ObstacleDirBundle(Obstacle o, float dir, float pointX, float pointY) {
        this.o = o;
        this.dir = dir;
        this.pointX = pointX;
        this.pointY = pointY;
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
