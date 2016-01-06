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

    // nano values for time measuring
    private long nanosToNextDirChange = 0, lastDirChangeNanos;
    // movement parameters!#############
    private float dirChangeRadiantsPerSec = (float) Math.PI / 4;
    //##############
    private float pixChangePerSec = 20;
    private float x, y, dir;
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
        float drawX = x + gm.getOffsetX();
        float drawY = y + gm.getOffsetY();
        c.drawCircle(drawX, drawY, SIZE, paint);
    }

    public void tick(long delayNanos) {
        nanosToNextDirChange = System.nanoTime() - lastDirChangeNanos;
        if (nanosToNextDirChange < 1) {
            nanosToNextDirChange = 500000 + (long) (Math.random() * 1000000);
            // change direction
            dirChangeRadiantsPerSec *= -1; // TODO vllt noch ändern, vllt auch mal null?
        }
        if (dirChangeRadiantsPerSec != 0) {
            dir += dirChangeRadiantsPerSec / (1000000 / delayNanos);
            dirSin = (float) Math.sin(dir);
            dirCos = (float) Math.cos(dir);
        }
        dir %= 2f * Math.PI;

        x += pixChangePerSec * dirCos;
        x += pixChangePerSec * dirSin;
    }
}

