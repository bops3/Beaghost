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

import java.util.Stack;

/**
 * Created by Florian on 03.01.2016.
 */
public class CustomDrawView extends SurfaceView implements SurfaceHolder.Callback {

    //private static final float LIFETIME = 60 * 10;
    private Paint aktCol;
    private DrawThread dt;
    private SurfaceHolder sh;
    private GameManager gm;
    private Obstacle[] obstacles;
    private Stack<Robot> robots;
    private float deltaX, deltaY;

    public CustomDrawView(Context contex) {
        super(contex);
    }

    public void init(GameManager gm, Obstacle[] obs, Stack<Robot> robs){
        this.gm = gm;
        obstacles = obs;
        robots = robs;
        sh = getHolder();
        sh.addCallback(this);
        dt = new DrawThread(sh);
        dt.start();
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
                    deltaX = x;
                    deltaY = y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    gm.setOffsetX(x - deltaX);
                    gm.setOffsetY(y - deltaY);
                    deltaX = x;
                    deltaY = y;
                    return true;
                case MotionEvent.ACTION_UP:
                    //Do nothing
                    return true;
            }
        else if (e.getPointerCount() == 2) {
            //TODO zoom
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


}

