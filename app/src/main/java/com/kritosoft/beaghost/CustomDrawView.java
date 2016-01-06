package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Stack;

/**
 * Created by Florian on 03.01.2016.
 */
public class CustomDrawView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int NONE = 0, DRAG = 1, ZOOM = 2;
    private int mode = 0;
    private boolean dragged = true;

    //...
    //private static final float LIFETIME = 60 * 10;
    private Context context;
    private Paint aktCol;
    private DrawThread dt;
    private SurfaceHolder sh;
    private GameManager gm;
    private Obstacle[] obstacles;
    private Stack<Robot> robots;
    private float deltaX, deltaY;
    private float startX, startY;
    private float previousTranslateX, previousTranslateY;
    private float mLastTouchX, mLastTouchY, mPosX, mPosY;
    private int mActivePointerId;
    private ScaleGestureDetector mScaleGestureDetector;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public CustomDrawView(Context context) {
        super(context);
        this.context = context;
    }

    public void init(GameManager gm, Obstacle[] obs, Stack<Robot> robs) {
        this.gm = gm;
        obstacles = obs;
        robots = robs;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gm.setScale(mScaleFactor);
        sh = getHolder();
        sh.addCallback(this);
        dt = new DrawThread(sh);
        dt.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        e.getPointerId(0);
//        Log.v("PointerCount: ", "" + e.getPointerCount());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                deltaX = x;
                deltaY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:

                gm.setOffsetX(x - deltaX);
                gm.setOffsetY(y - deltaY);
                break;

        }

        mScaleDetector.onTouchEvent(event);

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

    // stupid comment TODO

    public void setGameManager(GameManager gameManager) {
        gm = gameManager;
    }

    public void setObstacles(Obstacle[] obstacles) {
        this.obstacles = obstacles;
    }

    public void setRobots(Stack<Robot> robots) {
        this.robots = robots;
    }


    public class DrawThread extends Thread {

        Canvas c;
        SurfaceHolder sho;
        int col_b = Color.GREEN;

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
            c.scale(mScaleFactor, mScaleFactor);

            if ((deltaX * -1) < 0) {
                deltaX = 0;
            } else if ((deltaX * -1) > (mScaleFactor - 1) * displayWidth) {
                deltaX = (1 - mScaleFactor) * displayWidth;
            }

            if (deltaY * -1 < 0) {
                deltaY = 0;
            } else if ((deltaY * -1) > (mScaleFactor - 1) * displayHeight) {
                deltaY = (1 - mScaleFactor) * displayHeight;
            }

            c.translate(deltaX / mScaleFactor, deltaY / mScaleFactor);
            synchronized (this) {
                for (Obstacle o : obstacles) {
                    o.draw(c);
                }
                for (Robot r : robots) {
                    r.draw(c);
                }
                //draw here
            }
            return c;
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            gm.setScale(mScaleFactor);
            return true;
        }
    }


}

