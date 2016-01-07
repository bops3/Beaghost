package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Scanner;

public class Robot implements Drawable {
    // drawing
    public static final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final float pi = (float) Math.PI, a = 1.051650213f;
    private static final float[] angles; // Winkel für Ecken von Boxen

    Path drawPath = new Path(); // wiederverwendet für das Zeichnen der Boxen

    static {
        bodyPaint.setColor(0xffaabb77);
        pointerPaint.setColor(0xffaabb77);
        boxPaint.setColor(0xff000000);
        pointerPaint.setStrokeWidth(8f);
        // angles init
        angles = new float[8];
        angles[0] = pi / 4;
        angles[1] = a;
        angles[2] = pi - a;
        angles[3] = 0.75f * pi;
        angles[4] = 1.25f * pi;
        angles[5] = pi + a;
        angles[6] = 2 * pi - a;
        angles[7] = 1.75f * pi;
    }

    public final float radius = 15; // TODO Konstante!
    private final float fov = (float) (0.25 * Math.PI);
    private final float distA = (float) (Math.sqrt(2) * radius), distB = (float) (Math.sqrt(4.0625) * radius); // TODO anpassen, wenn sich radius ändert
    // Entfernungen zu den Ecken der Boxen zum Zeichnen

    private float[] angleSins = new float[8], angleCosins = new float[8];
    // nano values for time measuring
    private long millisFromLastDirChange = 0, lastDirChangeMillis, nextDirChangeDelayMillis;
    // movement parameters!#############
    private float maxDCRPS = (float) Math.PI, minDCRPS = (float) Math.PI / 4; // border values for dcrps
    private int minNDCDM = 700, maxNDCDM = 1400; // next direction change delay milliseconds
    private float minWCPS = 30f, maxWCPS = 400f; // way change per second (-> speed)
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
        changeDir(0f);
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

    public synchronized float getRadius() {
        return radius;
    }

    public synchronized float getX() {
        return x;
    }

    public synchronized float getY() {
        return y;
    }

    @Override
    public synchronized void draw(Canvas c) {
        pointerX = x + radius * 1.3f * dirCos;
        pointerY = y + radius * 1.3f * dirSin;

        // Blöcke links und rechts
        // rechter Block
        drawPath.reset();
        drawPath.moveTo(angleCosins[0] * distA + x, angleSins[0] * distA + y);
        drawPath.lineTo(angleCosins[1] * distB + x, angleSins[1] * distB + y);
        drawPath.lineTo(angleCosins[2] * distB + x, angleSins[2] * distB + y);
        drawPath.lineTo(angleCosins[3] * distA + x, angleSins[3] * distA + y);
        drawPath.close();
        c.drawPath(drawPath, boxPaint);
        // linker Block
        drawPath.reset();
        drawPath.moveTo(angleCosins[4] * distA + x, angleSins[4] * distA + y);
        drawPath.lineTo(angleCosins[5] * distB + x, angleSins[5] * distB + y);
        drawPath.lineTo(angleCosins[6] * distB + x, angleSins[6] * distB + y);
        drawPath.lineTo(angleCosins[7] * distA + x, angleSins[7] * distA + y);
        drawPath.close();
        c.drawPath(drawPath, boxPaint);

        c.drawLine(x, y, pointerX, pointerY, pointerPaint);
        c.drawCircle(x, y, radius, bodyPaint);
    }

    public synchronized void tick(long delayMillis) {

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
            changeDir(dirChangeRadiantsPerSec / framerate);
        }
        float wayChangeThisTick = wayChangePerSec / framerate;
        {
            float xW = x + wayChangeThisTick * dirCos;
            float yW = y + wayChangeThisTick * dirSin;
            if (!gm.isFree(xW, yW, this)) { // Weg ist nicht frei, also umdrehen
                changeDir(pi);
            }
        }
        // bewegen
        x += wayChangeThisTick * dirCos;
        y += wayChangeThisTick * dirSin;
    }

    private void changeDir(float value) {
        dir += value;
        dir %= 2 * pi;
        dirSin = (float) Math.sin(dir);
        dirCos = (float) Math.cos(dir);

        for (int i = 0; i < angles.length; i++) {
            float actangle = dir + angles[i];
            actangle %= 2 * pi;
            angleSins[i] = (float) Math.sin(actangle);
            angleCosins[i] = (float) Math.cos(actangle);
        }
    }

    public synchronized boolean sees(Robot r) {
        float m = (r.getY() - y) / (r.getX() - x);
        float angleToR = (float) Math.atan(m);
        float x;
        if (dir - fov / 2 < angleToR && dir + fov / 2 > angleToR) {
            //Robot liegt im Sichtfeld, es muss geprüft werden, ob hindernisse dazwischen liegen
            for (Obstacle o : gm.getObstacles()) {
                /**horizontal col. detection:
                 * line: y=m*x
                 * y = a
                 * => x = a/m, wenn x auf der Seite liegt, dann Kollision
                 */
                x = o.x;

            }


        }
        return false;
    }

}

