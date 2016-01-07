package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Scanner;

public class Robot implements Drawable {
    // ZEICHNEN +++++++++++++++++++++++++++++++++++++++++++++++
    // farben
    public static final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Paint viewFieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static final float pi = (float) Math.PI, a = 1.051650213f;
    private static final float[] angles; // Winkel für Ecken von Boxen

    static {
        bodyPaint.setColor(0xff990000);
        boxPaint.setColor(0xff000000);
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

    public final float radius = 15f; // TODO GRÖSSE!

    // Sichtfeld
    public final float viewfieldradius = 400f;

    private final float fov = (float) (0.25 * Math.PI);
    private final float distA = (float) (Math.sqrt(2) * radius), distB = (float) (Math.sqrt(4.0625) * radius); // TODO anpassen, wenn sich radius ändert

    private Path drawPath = new Path(); // wiederverwendet für das Zeichnen der Boxen
    // Entfernungen zu den Ecken der Boxen zum Zeichnen
    private float[] angleSins = new float[8], angleCosins = new float[8];

    // BEWEGUNG ++++++++++++++++++++++++++++++++++++++++++++++++++++++
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
    private float dirSin, dirCos;
    private GameManager gm;

    public Robot(float x, float y, float dir, GameManager gm) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        changeDir(0f);
        this.gm = gm;
        viewFieldPaint.setStyle(Paint.Style.FILL);
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
        // sichtfeld
        drawViewField(fov / 2, fov, c);
        // Körper
        c.drawCircle(x, y, radius, bodyPaint);
    }

    private void drawViewField(float angleFrom, float angle, Canvas c) {
        // Sichtfeldfarbverlauf an Winkel und Position anpassen
        RadialGradient gradient = new RadialGradient(x, y, viewfieldradius, new int[] { 0xccffffff, 0x00000000 }, null, Shader.TileMode.CLAMP);
        viewFieldPaint.setShader(gradient);
        RectF rectF = new RectF(x - viewfieldradius, y - viewfieldradius, x + viewfieldradius, y + viewfieldradius);
        c.drawArc(rectF, (float) Math.toDegrees(dir - angleFrom), (float) Math.toDegrees(angle), true, viewFieldPaint);
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

        // Werte für Boxen an den Seiten an Winkel anpassen
        for (int i = 0; i < angles.length; i++) {
            float actangle = dir + angles[i];
            actangle %= 2 * pi;
            angleSins[i] = (float) Math.sin(actangle);
            angleCosins[i] = (float) Math.cos(actangle);
        }
    }

    private synchronized/* ? */ void drawIntersectedField() {
        for (Obstacle o : gm.getObstacles()) {

        }
    }

    public synchronized boolean sees(Robot r) {
        float m = (r.getY() - y) / (r.getX() - x);
        float angleToR = (float) Math.atan(m);
        //Liegt der Punkt im Sichtfeld
        if (dir - fov / 2 < angleToR && dir + fov / 2 > angleToR)
            //Robot liegt im Sichtfeld, es muss geprüft werden, ob hindernisse dazwischen liegen
            for (Obstacle o : gm.getObstacles())
                //Es reicht 3 Kanten zu überprüfen, da immer mind 2 geschnitten werden
                if (sHLC(m, o.y, o.x, o.width) || sHLC(m, o.y + o.height, o.x, o.width) || sVLC(m, o.x, o.y, o.height))
                    return false;

        return true;
    }

    /**
     * simple horizontal line collision
     *
     * @return if they intersect
     */
    private boolean sHLC(float gradientA, float yB, float xB, float widthB) {
        /**horizontal col. detection:
         * A: line: y=m*x
         * B: y = a
         * => x = a/m, wenn x auf der Seite liegt, dann Kollision
         */
        float sx = yB / gradientA;
        return sx > xB && sx < xB + widthB;
    }

    /**
     * simple vertical line collision
     *
     * @return if they intersect
     */
    private boolean sVLC(float gradientA, float xB, float yB, float heightB) {
        /**vertical col. detection:
         * A: line: y=m*x
         * B: x = a
         * => y = m*a, wenn y auf der Seite liegt, dann Kollision
         */
        float sy = xB * gradientA;
        return sy > yB && sy < yB + heightB;
    }

}

