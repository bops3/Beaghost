package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    private SurfaceHolder sh;
    private GameManager gm;
    private float deltaX, deltaY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public CustomDrawView(Context context) {
        super(context);
        this.context = context;
    }

    public void init(GameManager gm) {
        this.gm = gm;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gm.setScale(mScaleFactor);
        sh = getHolder();
        sh.addCallback(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

//        e.getPointerId(0);
//        Log.v("PointerCount: ", "" + event.getPointerCount());
        switch (event.getAction()) {

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
                Log.d("CustomDrawView","ACTION_MOVE");
                gm.setOffsetX(x - deltaX);
                gm.setOffsetY(y - deltaY);
                deltaX =x;
                deltaY = y;
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



    public void redraw() {
        Canvas c = sh.lockCanvas();
        if (c != null) {
            sh.unlockCanvasAndPost(gm.drawCanvas(c));
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

