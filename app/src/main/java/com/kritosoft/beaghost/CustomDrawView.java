package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Florian on 03.01.2016.
 */
public class CustomDrawView extends SurfaceView implements SurfaceHolder.Callback {

    //private static final float LIFETIME = 60 * 10;
    int thickness = 4;
    Paint aktCol;
    DrawThread dt;
    ArrayList<Paint> paint = new ArrayList<>();
    private SurfaceHolder sh;
    private float steigung, lenght, dx, dy;
    private float startX = -1, startY = -1;
    private float aktX = -1, aktY = -1;
    private ArrayList<Float> aktpoints = new ArrayList<>(16);
    //    private ArrayList<Float> aktpArray;
    private ArrayList<ArrayList<Float>> points = new ArrayList<>();

    public CustomDrawView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
        changeColor(0xff0000ff);
        dt = new DrawThread(sh);
        dt.start();

    }

    public static float[] toPrim(Object[] f) {
        float[] a = new float[f.length];
        for (int i = 0; i < f.length; i++) {
            a[i] = (float) f[i];
        }
        return a;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        e.getPointerId(0);
//        Log.v("PointerCount: ", "" + e.getPointerCount());
        if (e.getPointerCount() == 1)
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = x;
                    startY = y;
                    aktpoints.add(x);
                    aktpoints.add(y);

                    paint.add(aktCol);

                    return true;
                case MotionEvent.ACTION_MOVE:
                    aktX = x;
                    aktY = y;
//                    aktpArray =aktpoints.toArray());
                    aktpoints.add(x);
                    aktpoints.add(y);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (paint.size() != 0) {//just removed all
                        aktpoints.add(x);
                        aktpoints.add(y);
//                    points.add(new Integer[]{startX, startY, x, y, LIFETIME});
                        if (aktpoints.size() % 4 != 0)
                            Log.d("CustomView", "aktpoints wrong size");
//                        float[] arrayPrim = toPrim(aktpoints.toArray(new Float[aktpoints.size()]));
                        synchronized (dt) {
                            points.add((ArrayList<Float>) aktpoints.clone());
                            aktpoints.clear();
                        }
                    }

//                    aktpArray = null;
                    startX = -1;
                    startY = -1;
                    aktX = -1;
                    aktY = -1;
                    return true;
            }
        else if (e.getPointerCount() == 3) {
            //remove all
            synchronized (dt) {
                points.clear();
                aktpoints.clear();
//                aktpArray = new float[0];
                paint.clear();
            }
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CutomView", "surfaceCreated");

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void changeColor(int col) {
        aktCol = new Paint(Paint.ANTI_ALIAS_FLAG);
        aktCol.setColor(col);

        aktCol.setStrokeWidth(thickness);
        Log.d("CustomView", "color set: " + Integer.toHexString(col));
    }

    public Canvas drawCanvas(Canvas c) {
        return dt.drawCanvas(c);
    }

    public int getAktColor() {
        return aktCol.getColor();
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int t) {
        thickness = t;
        changeColor(aktCol.getColor());
        Log.d("CustomView", "thickness set: " + t);
    }

    public class DrawThread extends Thread {

        Canvas c;
        SurfaceHolder sho;
        int col_b = Color.WHITE;

        public DrawThread(SurfaceHolder sho) {
            this.sho = sho;

        }

        @Override
        public void run() {

            while (!this.isInterrupted()) {

                c = sho.lockCanvas();
                if (c != null)
                    sho.unlockCanvasAndPost(drawCanvas(c));


                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    interrupt();
                }
            }

        }

        public Canvas drawCanvas(@NonNull Canvas c) {
            c.drawColor(col_b);

            // c.drawCircle(0, 0, y * (100 / (y + 10) + scaleY), paint);
            synchronized (this) {
                for (int k = 0; k < points.size(); k++) {
                    ArrayList<Float> p = points.get(k);
                    drawPath(c, p, paint.get(k));
                    //draw every point a circle

                }

//                    if (startX != -1 && aktX != -1)
//                        c.drawLine(startX, startY, aktX, aktY, paintSec);
                if (aktpoints != null && aktpoints.size() > 0)
                    drawPath(c, aktpoints, aktCol);
            }
            return c;
        }

        private void drawPath(Canvas c, ArrayList<Float> pnts, Paint p) {
            if (pnts.size() >= 4) {
                c.drawCircle(pnts.get(0), pnts.get(1), p.getStrokeWidth() / 2, p);
                for (int i = 0; (i + 3) < pnts.size(); i += 2) {
                    c.drawLine(pnts.get(i), pnts.get(i + 1), pnts.get(i + 2), pnts.get(i + 3), p);
//                    if (pnts.size() >= i + 3) {
                    c.drawCircle(pnts.get(i + 2), pnts.get(i + 3), p.getStrokeWidth() / 2, p);
//                    }
                }
            }
        }
    }


}

