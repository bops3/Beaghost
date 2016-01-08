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

public abstract class AbstractRobot implements Drawable {
    // STATIC ===========================================
    public final static float pi = (float) Math.PI, a = 1.051650213f;

    // ===================================================
    // GRÖSSEN +++++++++++++++++++++++
    public final float radius = 15f; // TODO GRÖSSE!
    public final float fov = (float) (0.25 * Math.PI);
    // Sichtfeld
    public final float viewfieldradius = 400f;
    // SICHTFELD ZEICHNEN &&&&&&&&&&&&&&&&&&&
    public final int viewFieldColor = 0xccffff77;
    public final Paint viewFieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // ++++++++++++++++++++++++++++++
    // BEWEGUNG ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected float wayChangePerSec = 100;
    protected float x, y;
    protected GameManager gm;
    //### temp vars #######
    private float tempAngle;
    private Obstacle tempTouch, tempIntersect;
    private float dir, dirSin, dirCos;

    // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&
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

    protected void drawViewField(float angleFrom, float angle, Canvas c) {
        // Sichtfeldfarbverlauf an Winkel und Position anpassen
        RadialGradient gradient = new RadialGradient(x, y, viewfieldradius, new int[]{0xccffffff, 0x00000000}, null, Shader.TileMode.CLAMP);
        viewFieldPaint.setShader(gradient);
        drawIntersectedField(c);
    }

    private void drawCircSector(float angel1, float angel2, Canvas c) {

        RectF rectF = new RectF(x - viewfieldradius, y - viewfieldradius, x + viewfieldradius, y + viewfieldradius);
        c.drawArc(rectF, (float) Math.toDegrees(angel1), (float) Math.toDegrees(angel2), true, viewFieldPaint);
    }

    private void drawTrinangle(float x0, float y0, float x1, float y1, Canvas c) {
        Path p = new Path();
        p.reset();
        p.moveTo(x0, y0);
        p.lineTo(x1, y1);
        p.lineTo(x, y);
        p.close();
        c.drawPath(p, viewFieldPaint);

    }


    private void drawIntersectedField(Canvas c) {
        LinkedList<ObstacleDirBundle> list = new LinkedList<>();
        for (Obstacle o : gm.getObstacles()) {
            if (isInView(o.x, o.y))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x, o.y, isTouchAngle(tempAngle, 0)));
            if (isInView(o.x + o.width, o.y))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x + o.width, o.y, isTouchAngle(tempAngle, 1)));
            if (isInView(o.x, o.y + o.height))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x, o.y + o.height, isTouchAngle(tempAngle, 2)));
            if (isInView(o.x + o.width, o.y + o.height))
                list.add(new ObstacleDirBundle(o, tempAngle, o.x + o.width, o.y + o.height, isTouchAngle(tempAngle, 3)));

        }
        Collections.sort(list, new Comparator<ObstacleDirBundle>() {
            @Override
            public int compare(ObstacleDirBundle lhs, ObstacleDirBundle rhs) {
                return (int) Math.signum(lhs.getDir() - rhs.getDir());
            }
        });
        //ray durch erste Sichtfeldbegrenzung
        float mRay = getGradientfromAngle(dir - fov / 2);
        list.addFirst(new ObstacleDirBundle(null, dir - fov / 2, 0, 0, false));
        list.addLast(new ObstacleDirBundle(null, dir + fov / 2, 0, 0, false));

        float[][] returnArray;
        float[] aktBestBPunkt = null;//berührt
        float[] aktBestSPunkt = null;//schneidet
        float[] lastBestBPunkt;
        float[] lastBestSPunkt;

        Obstacle lastT;
        Obstacle lastI;
        Obstacle aktT = null;
        Obstacle aktI = null;
        float lastAngel;
        float aktAngel = getAngle(mRay);

        for (int i = 0; i < list.size(); i++) {
            lastT = aktT;
            lastI = aktI;
            lastBestBPunkt = aktBestBPunkt;
            lastBestSPunkt = aktBestSPunkt;
            lastAngel = aktAngel;
            aktAngel = list.get(i).getDir();
            float m = getGradientfromAngle(aktAngel);
            returnArray = getFirstHitInRadius(m, list);
            aktBestBPunkt = returnArray[0];
            aktBestSPunkt = returnArray[1];
            aktT = tempTouch;
            aktI = tempIntersect;
            if (lastI != null) {
                if (lastI == aktI)
                    //zweimal schneiden
                    drawTrinangle(lastBestSPunkt[0], lastBestSPunkt[1], aktBestSPunkt[0], aktBestSPunkt[1], c);
                else if (aktI == null)
                    //intersect, dann touch
                    drawTrinangle(lastBestSPunkt[0], lastBestSPunkt[1], aktBestBPunkt[0], aktBestBPunkt[1], c);

            } else if (lastT != null) {
                if (aktI == null)
                    //nur touch
                    drawCircSector(lastAngel, aktAngel, c);
                else
                    //touch, dann intersect
                    drawTrinangle(lastBestBPunkt[0], lastBestBPunkt[1], aktBestSPunkt[0], aktBestSPunkt[1], c);
            }
            //ray tracen, wenn hit in bereich dann dreieck, sonst bogen
        }

    }

    private float[][] getFirstHitInRadius(float mRay, LinkedList<ObstacleDirBundle> list) {
        Obstacle obs = null;
        LinkedList<float[]> sPunkteList;
        float[] bestBPunkt = null;//berührt
        float[] bestSPunkt = null;//schneidet
        tempIntersect = null;
        tempTouch = null;
//        boolean rayHitsST = false;
//        boolean rayHits2T = false;
        float smallestDis = viewfieldradius;
        float smallestDisB = viewfieldradius;
        float newDis;
        int whichCorner;
        for (ObstacleDirBundle odb : list) {
            if (odb.getO() == obs)
                continue;
            obs = odb.getO();
            if ((sPunkteList = rayHitsObstacleAt(mRay, obs)).size() != 0)
                for (float[] sPunkt : sPunkteList) {
                    newDis = absDis(sPunkt[0], sPunkt[1]);
                    if ((whichCorner = whichCornerPoint(obs, sPunkt[0], sPunkt[1])) != -1 && isTouchAngle(getAngle(mRay), whichCorner)) {
                        if (newDis < smallestDisB && newDis < smallestDis) {
                            smallestDisB = newDis;
                            bestBPunkt = sPunkt;
                            tempTouch = obs;
                        }
                    } else {
//                        if (newDis < viewfieldradius && rayHitsST)
//                            rayHits2T = true;
                        if (newDis < smallestDis) {
                            smallestDis = newDis;
                            bestSPunkt = sPunkt;
                            tempIntersect = obs;
                            if (newDis < smallestDisB) {
                                smallestDisB = newDis;
                                bestBPunkt = null;
                                tempTouch = null;
                            }
//                            rayHitsST = true;
                        }
                    }
                }
        }
        return new float[][]{bestBPunkt, bestSPunkt};
    }

    private boolean isTouchAngle(float angle, int whichCorner) {
        switch (whichCorner) {
            case 0:
            case 3:
                return (angle > 0 && angle < 0.5 * pi) || (angle > pi & angle < 1.5 * pi);
            default:
                return (angle > 0.5 * pi && angle < pi) || (angle > 1.5 * pi & angle < 2 * pi);
        }
    }

    /**
     * @return -1: no corner, 0 top-left, 1 top-right, 2 bot-left, 3 bot-right
     */
    private int whichCornerPoint(Obstacle o, float x, float y) {
        int ret;
        if (y == o.y)
            ret = 0;
        else if (y == o.y + o.height)
            ret = 2;
        else
            return -1;

        if (x == o.x + o.width)
            return ret & 1;
        else if (x == o.x)
            return ret;
        else
            return -1;
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
        return sx >= xB && sx <= xB + widthB;
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
        return sy >= yB && sy <= yB + heightB;
    }

    private float sVLCAt(float gradientA, float xB) {
        return xB * gradientA;
    }

}
