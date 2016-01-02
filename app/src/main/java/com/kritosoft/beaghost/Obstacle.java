package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Scanner;

/**
 * Created by Florian on 02.01.2016.
 */
public class Obstacle implements Drawable {

    public static final String DELIM_OS = " ";
    public static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        paint.setColor(0x000000ff);
    }

    private float x, y, width, height;

    public Obstacle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Obstacle createFromFile(String line) {

        Scanner sc = new Scanner(line).useDelimiter(DELIM_OS);
        float x, y, width, height;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        width = Float.parseFloat(sc.next());
        height = Float.parseFloat(sc.next());
        return new Obstacle(x, y, width, height);
    }

    @Override
    public void draw(Canvas c) {
        c.drawRect(x, y, width, height, paint);
    }
}
