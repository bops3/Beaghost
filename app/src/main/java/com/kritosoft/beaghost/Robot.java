package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Scanner;

/**
 * Created by Florian on 02.01.2016.
 */
public class Robot implements Drawable {
    public static final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static final float SIZE = 20;

    static {
        bodyPaint.setColor(0xff0000ff);
        pointerPaint.setColor(0xffccdd00);
    }

    // nano values for time measuring
    private long millisFromLastDirChange = 0, lastDirChangeMillis;
    // movement parameters!#############
    private float dirChangeRadiantsPerSec = 1f;
    private long nextDirChangeDelayMillis = 1000;
    //##############
    private float pixChangePerSec = 300;
    private float x, y, dir;
    private float drawX, drawY, scale, pointerX, pointerY;
    private float dirSin, dirCos;
    private GameManager gm;

    public Robot(float x, float y, float dir, GameManager gm) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.gm = gm;
    }

    public static Robot createFromFile(String line, GameManager gm) {

        Scanner sc = new Scanner(line).useDelimiter(GameManager.DELIM);
        float x, y, dir;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        dir = Float.parseFloat(sc.next());
        return new Robot(x, y, dir, gm);
    }

    @Override
    public void draw(Canvas c) {
//        scale = gm.getScale();
//        drawX = (x + gm.getOffsetX()) * scale;
//        drawY = (y + gm.getOffsetY()) * scale;
//
//        pointerX = (x + SIZE * 3 * dirCos + gm.getOffsetX()) * scale;
//        pointerY = (y + SIZE * 3 * dirSin + gm.getOffsetY()) * scale;
//
//        c.drawLine(drawX, drawY, pointerX, pointerY, pointerPaint);
//        c.drawCircle(drawX, drawY, SIZE * scale, bodyPaint);


//        pointerX = (x + SIZE * 3 * dirCos + gm.getOffsetX()) * scale;
//        pointerY = (y + SIZE * 3 * dirSin + gm.getOffsetY()) * scale;
//
//        c.drawLine(drawX, drawY, pointerX, pointerY, pointerPaint);
        Log.v("Robot", "draw");
        c.drawCircle(x, y, SIZE, bodyPaint);
    }

    public void tick(long delayMillis) {
        Log.v("Robot", "tick");

        millisFromLastDirChange = System.currentTimeMillis() - lastDirChangeMillis;
        // Richtungsänderung ändern?
        if (millisFromLastDirChange > nextDirChangeDelayMillis) {
            lastDirChangeMillis = System.currentTimeMillis();
            millisFromLastDirChange = 0;
            nextDirChangeDelayMillis = 500 + (long) (Math.random() * 1000);

            // change direction TODO Beträge anpassen!!!
            dirChangeRadiantsPerSec *= -1;
            if (Math.random() < 0.333d) {
                dirChangeRadiantsPerSec = 0;
            } else {
                dirChangeRadiantsPerSec = (1f + (float) Math.random());
                if (Math.random() < 0.5f)
                    dirChangeRadiantsPerSec *= -1;
            }
        }
        float framerate = 1000 / delayMillis;
        // wenn änderung nötig, dann ändern
        if (dirChangeRadiantsPerSec != 0) {
            dir += dirChangeRadiantsPerSec / framerate;
            dir %= 2f * Math.PI;
            dirSin = (float) Math.sin(dir);
            dirCos = (float) Math.cos(dir);
        }
        // bewegen
        x += (pixChangePerSec / framerate) * dirCos;
        y += (pixChangePerSec / framerate) * dirSin;
    }
}

