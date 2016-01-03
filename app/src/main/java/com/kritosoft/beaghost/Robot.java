package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Scanner;

/**
 * Created by Florian on 02.01.2016.
 */
public class Robot implements Drawable {
    public static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final float SIZE = 20;

    static {
        paint.setColor(0xff0000ff);
    }

    private float x, y, dir;

    public Robot(float x, float y, float dir) {
        this.x = x;
        this.y = y;
        this.dir = dir;
    }

    public static Robot createFromFile(String line) {

        Scanner sc = new Scanner(line).useDelimiter(GameManager.DELIM);
        float x, y, dir;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        dir = Float.parseFloat(sc.next());
        return new Robot(x, y, dir);
    }

    @Override
    public void draw(Canvas c) {
        c.drawCircle(x, y, SIZE, paint);
    }
}

