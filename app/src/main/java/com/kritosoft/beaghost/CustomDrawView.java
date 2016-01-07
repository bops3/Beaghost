package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Florian on 03.01.2016.
 */
public class CustomDrawView extends SurfaceView implements SurfaceHolder.Callback {

    private boolean nonPrimPointerRelaesed = false;
    private float pointerX, pointerY;
    //private static final float LIFETIME = 60 * 10;
    private Context context;
    private SurfaceHolder sh;
    private GameManager gm;
    private float deltaX, deltaY;
    private int width, height;

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
                Log.v("CustomDrawView", "ACTION_DOWN");
                pointerX = x;
                pointerY = y;
                deltaX = x;
                deltaY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.v("CustomDrawView", "ACTION_POINTER_DOWN");
                deltaX = x;
                deltaY = y;
                break;
            case MotionEvent.ACTION_UP:
                Log.v("CustomDrawView", "ACTION_UP");
                if (nonPrimPointerRelaesed)
                    nonPrimPointerRelaesed = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.v("CustomDrawView", "ACTION_POINTER_UP");
                nonPrimPointerRelaesed = true;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d("CustomDrawView", "ACTION_MOVE");
                if (nonPrimPointerRelaesed) {
                    nonPrimPointerRelaesed = false;
                } else {
                    gm.changeOffsetX(x - deltaX);
                    gm.changeOffsetY(y - deltaY);
                }
                deltaX = x;
                deltaY = y;

                break;

        }

        mScaleDetector.onTouchEvent(event);

        return true;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CutomView", "surfaceCreated");
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Log.d("CustomDrawView", "size of view: " + width + ", " + height);
        gm.setViewSize(width,height);
        gm.startTicking();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gm.stopTicking();

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
            float sf = detector.getScaleFactor();
            mScaleFactor *= sf;
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
//            Log.v("CustomDrawView", "scale: " + mScaleFactor);
            gm.setScale(mScaleFactor);
            return true;
        }
    }


}

