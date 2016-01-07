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
    private static final float[] angles; // angle constants

    static {
        bodyPaint.setColor(0xff0000ff);
        pointerPaint.setColor(0xffccdd00);
        boxPaint.setColor(0xff000000);
        pointerPaint.setStrokeWidth(5f);
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
    private final float distA = (float) (Math.sqrt(2) * radius), distB = (float) (Math.sqrt(4.0625) * radius); // TODO anpassen, wenn sich radius ändert
    private float[] angleSins = new float[8], angleCosins = new float[8];
    // nano values for time measuring
    private long millisFromLastDirChange = 0, lastDirChangeMillis, nextDirChangeDelayMillis;
    // movement parameters!#############
    private float maxDCRPS = (float) Math.PI, minDCRPS = (float) Math.PI / 4; // border values for dcrps
    private int minNDCDM = 700, maxNDCDM = 1400; // next direction change delay milliseconds
    private float minWCPS = 0f, maxWCPS = 0f; // way change per second (-> speed)
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
        dirChanged();
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
// TODO synchronizen
    @Override
    public void draw(Canvas c) {
        pointerX = x + radius * 1.3f * dirCos;
        pointerY = y + radius * 1.3f * dirSin;

        // Blöcke links und rechts
        float[] pp = new float[8];
        Path p = new Path();
        // rechter Block
        pp[0] = angleCosins[0]*distA + x;
        pp[1] = angleSins[0] * distA + y;
        pp[2] = angleCosins[1] * distB + x;
        pp[3] = angleSins[1] * distB + y;
        pp[4] = angleCosins[2] * distB + x;
        pp[5] = angleSins[2] * distB + y;
        pp[6] = angleCosins[3] * distA + x;
        pp[7] = angleSins[3] * distA + y;
        p.reset();
        p.moveTo(pp[0], pp[1]);
        p.lineTo(pp[2], pp[3]);
        p.lineTo(pp[4], pp[5]);
        p.lineTo(pp[6], pp[7]);
        p.close();
        c.drawPath(p, boxPaint);
        // linker Block
        pp[0] = angleCosins[4]*distA + x;
        pp[1] = angleSins[4] * distA + y;
        pp[2] = angleCosins[5] * distB + x;
        pp[3] = angleSins[5] * distB + y;
        pp[4] = angleCosins[6] * distB + x;
        pp[5] = angleSins[6] * distB + y;
        pp[6] = angleCosins[7] * distA + x;
        pp[7] = angleSins[7] * distA + y;
        p.reset();
        p.moveTo(pp[0], pp[1]);
        p.lineTo(pp[2], pp[3]);
        p.lineTo(pp[4], pp[5]);
        p.lineTo(pp[6], pp[7]);
        p.close();
        c.drawPath(p, boxPaint);

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
            dirChanged();
        }
        float wayChangeThisTick = wayChangePerSec / framerate;
        {
            float xW = x + wayChangeThisTick * dirCos;
            float yW = y + wayChangeThisTick * dirSin;
            if (!gm.isFree(xW, yW, this)) { // Weg ist nicht frei, also umdrehen
                dir += Math.PI;
                dir %= 2f * Math.PI;
                dirChanged();
            }
        }
        // bewegen
        x += wayChangeThisTick * dirCos;
        y += wayChangeThisTick * dirSin;
    }

    private void dirChanged() {
        dirSin = (float) Math.sin(dir);
        dirCos = (float) Math.cos(dir);

        for (int i = 0; i < angles.length; i++) {
            float actangle = dir + angles[i];
            actangle %= 2 * pi;
            angleSins[i] = (float) Math.sin(actangle);
            angleCosins[i] = (float) Math.cos(actangle);
        }
    }
}

