package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Scanner;

/**
 * Created by Florian on 02.01.2016.
 */
public class Obstacle implements Drawable {

    public static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        paint.setColor(0x000000ff);
    }

    private GameManager gm;
    private float x, y, width, height;

    public Obstacle(float x, float y, float width, float height, GameManager gm) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.gm = gm;
    }

    public static Obstacle createFromFile(String line, GameManager gm) {

        Scanner sc = new Scanner(line).useDelimiter(GameManager.DELIM);
        float x, y, width, height;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        width = Float.parseFloat(sc.next());
        height = Float.parseFloat(sc.next());
        Log.d("Obstacle","Obstacle erstellt: "+x+", "+y+" | "+width+", "+height);
        return new Obstacle(x, y, width, height, gm);
    }

    @Override
    public void draw(Canvas c) {
        Log.v("Obstacle", "drawing obstacle" +( x - gm.getOffsetX())+", "+(y - gm.getOffsetY()));
        c.drawRect(x - gm.getOffsetX(), y - gm.getOffsetY(), width, height, paint);
    }
}
