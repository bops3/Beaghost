package com.kritosoft.beaghost;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

public class Robot implements Drawable {
    // ZEICHNEN +++++++++++++++++++++++++++++++++++++++++++++++
    // farben
    public static final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

    public final Paint viewFieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
    //### temp vars #######
    private float tempAngle;
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
        RadialGradient gradient = new RadialGradient(x, y, viewfieldradius, new int[]{0xccffffff, 0x00000000}, null, Shader.TileMode.CLAMP);
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

    private void drawIntersectedField() {
        LinkedList<ObstacleDirBundle> list = new LinkedList<>();
        for (Obstacle o : gm.getObstacles()) {
            if (isInView(o.x, o.y))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x, o.y));
            if (isInView(o.x + o.width, o.y))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x + o.width, o.y));
            if (isInView(o.x, o.y + o.height))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x, o.y + o.height));
            if (isInView(o.x + o.width, o.y + o.height))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x + o.width, o.y + o.height));

        }
        Collections.sort(list, new Comparator<ObstacleDirBundle>() {
            @Override
            public int compare(ObstacleDirBundle lhs, ObstacleDirBundle rhs) {
                return (int) Math.signum(lhs.getDir() - rhs.getDir());
            }
        });
        //ray durch erste Sichtfeldbegrenzung
        float mRay = getGradientfromAngle(dir - fov / 2);
        getFirstHit(mRay, list);

        for (ObstacleDirBundle odb : list) {
            float pX = odb.getPointX();
            float pY = odb.getPointY();
            float m = getGradient(pX, pY);
            //ray tracen, wenn hit in bereich dann dreieck, sonst bogen
        }

    }

    private void getFirstHit(float mRay, LinkedList<ObstacleDirBundle> list) {
        Obstacle obs = null;
        LinkedList<float[]> sPunkteList;
        float[] bestSPunkt = null;
        boolean rayHitsST = false;
        boolean rayHits2T = false;
        float smallestDis = Float.MAX_VALUE;
        float newDis;
        for (ObstacleDirBundle odb : list) {
            if (odb.getO() == obs)
                continue;
            obs = odb.getO();
            if ((sPunkteList = rayHitsObstacleAt(mRay, obs)).size() != 0)
                for (float[] sPunkt : sPunkteList)
                    if ((newDis = absDis(sPunkt[0], sPunkt[1])) < smallestDis) {
                        smallestDis = newDis;
                        bestSPunkt = sPunkt;
                        if (rayHitsST)
                            rayHits2T = true;
                        else
                            rayHitsST = true;
                    }
        }
    }

    public synchronized boolean sees(Robot r) {
        float m = getGradient(r.getX(), r.getY());
        float angleToR = getAngle(m);
        //Liegt der Punkt im Sichtfeld
        if (isAngleInFOV(angleToR))
            //Robot liegt im Sichtfeld, es muss geprüft werden, ob hindernisse dazwischen liegen
            for (Obstacle o : gm.getObstacles())
                if (rayHitsObstacle(m, o))
                    return false;

        return true;
    }

    private boolean rayHitsObstacle(float mRay, Obstacle o) {
        //Es reicht 3 Kanten zu überprüfen, da immer mind 2 geschnitten werden
        //Ursprung ist x,y
        return sHLC(mRay, o.y - y, o.x - x, o.width) || sHLC(mRay, o.y - y + o.height, o.x - x, o.width) || sVLC(mRay, o.x - x, o.y - y, o.height);
    }

    private LinkedList<float[]> rayHitsObstacleAt(float mRay, Obstacle o) {
        //alle 4 Kanten, da Schnittpunkte gesucht sind
        LinkedList<float[]> list = new LinkedList<>();

        float sx = sHLCAt(mRay, o.y - y);
        if (sHLC(sx, o.x - x, o.width))
            list.add(new float[]{sx, o.y - y});

        sx = sHLCAt(mRay, o.y + o.height - y);
        if (sHLC(sx, o.x - x, o.width))
            list.add(new float[]{sx, o.y + o.height - y});

        float sy = sVLCAt(mRay, o.x - x);
        if (sVLC(sy, o.y - y, o.height))
            list.add(new float[]{o.x - x, sy});

        sy = sVLCAt(mRay, o.x + o.width - x);
        if (sVLC(sy, o.y - y, o.height))
            list.add(new float[]{o.x + o.width - x, sy});

        return list;
    }

    private boolean isAngleInFOV(float angle) {
        return dir - fov / 2 < angle && dir + fov / 2 > angle;
    }

    private float getGradient(float x, float y) {
        return (y - this.y) / (x - this.x);
    }

    private float getGradientfromAngle(float angle) {
        return (float) Math.tan(angle);
    }

    private float getAngle(float gradient) {
        tempAngle = (float) Math.atan(gradient);
        return tempAngle;
    }

    private float getDistance(float x, float y) {
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
    }

    private float absDis(float x, float y) {
        return Math.abs(x - this.x) + Math.abs(y - this.y);
    }

    private boolean isInView(float x, float y) {
        return isAngleInFOV(getAngle(getGradient(x, y))) && getDistance(x, y) <= viewfieldradius;
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
        float sx = sHLCAt(gradientA, yB);
        return sHLC(sx, xB, widthB);
    }

    private boolean sHLC(float sx, float xB, float widthB) {
        return sx > xB && sx < xB + widthB;
    }

    private float sHLCAt(float gradientA, float yB) {
        return yB / gradientA;
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
        float sy = sVLCAt(gradientA, xB);
        return sVLC(sy, yB, heightB);
    }

    private boolean sVLC(float sy, float yB, float heightB) {
        return sy > yB && sy < yB + heightB;
    }

    private float sVLCAt(float gradientA, float xB) {
        return xB * gradientA;
    }

}

