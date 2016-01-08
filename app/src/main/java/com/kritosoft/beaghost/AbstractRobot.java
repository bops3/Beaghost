package com.kritosoft.beaghost;

import android.graphics.Canvas;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public abstract class AbstractRobot implements Drawable {
    // STATIC ===========================================
    public final static float pi = (float) Math.PI, a = 1.051650213f;
    // ===================================================
    // GRÖSSEN +++++++++++++++++++++++
    public final float radius = 15f; // TODO GRÖSSE!
    public final float fov = (float) (0.25 * Math.PI);
    // Sichtfeld
    public final float viewfieldradius = 400f;
    // ++++++++++++++++++++++++++++++

    //### temp vars #######
    private float tempAngle;

    // BEWEGUNG ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected float wayChangePerSec = 100;
    protected float x, y;
    protected GameManager gm;
    private float dir, dirSin, dirCos;

    public AbstractRobot(float x, float y, float dir, GameManager gm) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        changeDir(0f);
        this.gm = gm;
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

    public synchronized float getDirCos() {
        return dirCos;
    }

    public synchronized float getDirSin() {
        return dirSin;
    }

    public synchronized float getDir() {
        return dir;
    }


    protected void changeDir(float change) {
        dir += change;
        dir %= 2 * pi;
        dirSin = (float) Math.sin(dir);
        dirCos = (float) Math.cos(dir);
    }


    /**
     * Must be SYNCHRONIZED with super.this!!!!!!!!
     *
     * @param delayMillis
     */
    public abstract void tick(long delayMillis);

    public abstract void draw(Canvas c);


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
        return getDir() - fov / 2 < angle && getDir() + fov / 2 > angle;
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
