package com.kritosoft.beaghost;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

public class Robot extends SimpleRobot {
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

    public Robot(float x, float y, float dir, GameManager gm) {
        super(x, y, dir, gm, 0xff990000);
    }

    public static Robot createFromFile(String line, GameManager gm) {

        Scanner sc = new Scanner(line).useDelimiter(GameManager.DELIM);
        float x, y, dir;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        dir = Float.parseFloat(sc.next());
        return new Robot(x, y, dir, gm);
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
            float xW = x + wayChangeThisTick * getDirCos();
            float yW = y + wayChangeThisTick * getDirSin();
            if (!gm.isFree(xW, yW, this)) { // Weg ist nicht frei, also umdrehen
                changeDir(pi);
            }
        }
        // bewegen
        x += wayChangeThisTick * getDirCos();
        y += wayChangeThisTick * getDirSin();
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

        for (ObstacleDirBundle odb : list) {
            float pX = odb.getPointX();
            float pY = odb.getPointY();
            float m = getGradient(pX, pY);
            //ray tracen, wenn hit in bereich dann dreieck, sonst bogen
        }

    }

    public synchronized boolean sees(Robot r) {
        float m = getGradient(r.getX(), r.getY());
        float angleToR = getAngle(m);
        //Liegt der Punkt im Sichtfeld
        if (isAngleInFOV(angleToR))
            //Robot liegt im Sichtfeld, es muss geprüft werden, ob hindernisse dazwischen liegen
            for (Obstacle o : gm.getObstacles())
                //Es reicht 3 Kanten zu überprüfen, da immer mind 2 geschnitten werden
                if (sHLC(m, o.y, o.x, o.width) || sHLC(m, o.y + o.height, o.x, o.width) || sVLC(m, o.x, o.y, o.height))
                    return false;

        return true;
    }

    private boolean isAngleInFOV(float angle) {
        return getDir() - fov / 2 < angle && getDir() + fov / 2 > angle;
    }

    private float getGradient(float x, float y) {
        return (y - this.y) / (x - this.x);
    }

    private float getAngle(float gradient) {
        tempAngle = (float) Math.atan(gradient);
        return tempAngle;
    }

    private float getDistance(float x, float y) {
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
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

