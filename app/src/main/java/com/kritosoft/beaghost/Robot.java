package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Scanner;

public class Robot implements Drawable {
    public static final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        bodyPaint.setColor(0xff0000ff);
        pointerPaint.setColor(0xffccdd00);
        pointerPaint.setStrokeWidth(5f);
    }

    private float radius = 15;
    // nano values for time measuring
    private long millisFromLastDirChange = 0, lastDirChangeMillis, nextDirChangeDelayMillis;
    // movement parameters!#############
    private float maxDCRPS = (float) Math.PI, minDCRPS = (float) Math.PI / 4; // border values for dcrps
    private int minNDCDM = 700, maxNDCDM = 1400; // next direction change delay milliseconds
    private float minWCPS = 50f, maxWCPS = 550f; // way change per second (-> speed)
    // automatisch erstellte Momentanwerte (aus Bereichen zufällig generiert)
    private float dirChangeRadiantsPerSec = 1f; // direction change radiants per second (dcrps)
    private float wayChangePerSec = 300; // (-> speed)
    //##############
    private float x, y, dir;
    private float pointerX, pointerY;
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

    public float getRadius() {
        return radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public void draw(Canvas c) {
        pointerX = x + radius * 3 * dirCos;
        pointerY = y + radius * 3 * dirSin;

        c.drawLine(x, y, pointerX, pointerY, pointerPaint);
        c.drawCircle(x, y, radius, bodyPaint);
    }

    public void tick(long delayMillis) {

        millisFromLastDirChange = System.currentTimeMillis() - lastDirChangeMillis;
        if (millisFromLastDirChange > nextDirChangeDelayMillis) {
            // Richtung (un andere Parameter) ändern
            lastDirChangeMillis = System.currentTimeMillis();
            millisFromLastDirChange = 0;
            nextDirChangeDelayMillis = minNDCDM + (long) (Math.random() * (maxNDCDM - minNDCDM));

            // change direction
            dirChangeRadiantsPerSec *= -1;
            if (Math.random() < 0.333d) { // TODO Wahrscheinlichkeiten anpassen..
                dirChangeRadiantsPerSec = 0;
            } else {
                dirChangeRadiantsPerSec = minDCRPS + (float) Math.random() * (maxDCRPS - minDCRPS);
                if (Math.random() < 0.5f)
                    dirChangeRadiantsPerSec *= -1;
            }
            // change speed
            wayChangePerSec = minWCPS + (float) Math.random() * (maxWCPS - minWCPS);
        }
        float framerate = 1000 / delayMillis;
        // wenn änderung nötig, dann ändern
        if (dirChangeRadiantsPerSec != 0) {
            dir += dirChangeRadiantsPerSec / framerate;
            dir %= 2f * Math.PI;
            dirSin = (float) Math.sin(dir);
            dirCos = (float) Math.cos(dir);
        }
        float wayChangeThisTick = wayChangePerSec / framerate;
        {
            float xW = x + wayChangeThisTick * dirCos;
            float yW = y + wayChangeThisTick * dirSin;
            if (!gm.isFree(xW, yW, this)) { // Weg ist nicht frei, also umdrehen
                dir += Math.PI;
                dir %= 2f * Math.PI;
                dirSin = (float) Math.sin(dir);
                dirCos = (float) Math.cos(dir);
            }
        }
        // bewegen
        x += wayChangeThisTick * dirCos;
        y += wayChangeThisTick * dirSin;
    }
}

