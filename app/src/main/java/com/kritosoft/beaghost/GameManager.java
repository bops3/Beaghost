package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by Florian on 02.01.2016.
 */
public class GameManager {
    // sinnloser kommentar von Floooo
    public static final byte TYPE_OBSTACLE = 0, TYPE_ROBOT = 1;
    public static final String DELIM = " ";
    public static final int drawTPS = 60, tickTPS = 60;
    int col_b = 0xff000000;
    Bitmap background;
    private Clock drawClock, tickClock;
    private Obstacle[] obstacles;
    private Stack<Robot> robots = new Stack<>();
    private Context context;
    private CustomDrawView cdv;
    //offset and mapsize are measured in the same unit.
    //screenSize unit is different!(depends on scale)
    private int mapWidth, mapHeight, offsetX, offsetY, offsetMaxX, offsetMaxY, offsetMinY = 0, offsetMinX = 0, screenX, screenY;
    private float scale;
    private Paint paint_mapBack = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        paint_mapBack.setColor(0xffcccccc);
    }

    public GameManager(Context context) {
        this.context = context;
        genMapFromFile(R.raw.map1);
        Log.d("GameManager", "creating CustomDrawView...");
        cdv = new CustomDrawView(context);
        Log.d("GameManager", "CustomDrawView created!");
        cdv.init(this);
        Log.d("GameManager", "CustomDrawView init finished.");
//        background = prepareBackground();

        drawClock = new Clock(drawTPS, new Tickable() {
            @Override
            public void tick(int millisDelta) {
                cdv.redraw();
            }
        });
        tickClock = new Clock(tickTPS, new Tickable() {
            @Override
            public void tick(int millisDelta) {
                tickAll(millisDelta);
            }
        });
        Log.d("GameManager", "GameManager finished.");
    }

    private void genMapFromFile(int mapId) {

        InputStreamReader isr = new InputStreamReader(context.getResources().openRawResource(mapId));
        Scanner reader = new Scanner(isr);
        int anzObstacles, i = 0;

        mapWidth = Integer.parseInt(reader.next());
        mapHeight = Integer.parseInt(reader.next());
        Log.d("GameManager", "Map size: " + mapWidth + ", " + mapHeight);
        String anzOStr = reader.next().trim();
        anzObstacles = Integer.parseInt(anzOStr);

        obstacles = new Obstacle[anzObstacles];

        while (reader.hasNext()) {
            byte type = Byte.parseByte(reader.next());
            switch (type) {
                case TYPE_OBSTACLE:
                    obstacles[i] = Obstacle.createFromFile(reader.nextLine(), this);
                    i++;
                    break;
                case TYPE_ROBOT:
                    robots.add(Robot.createFromFile(reader.nextLine(), this));
                    break;
                default:
                    reader.nextLine();
            }
        }

    }

    public int getOffsetX() {
        return offsetX;
    }

    public void changeOffsetX(float change) {
        offsetX += change / scale;
        if (offsetX > offsetMinX)//offset: -123, offsetMinx = 0
            offsetX = offsetMinX;
        else if (offsetX < offsetMaxX)//offset: -123, offsetMax : -1000
            offsetX = offsetMaxX;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        setMaxOffset();
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void changeOffsetY(float change) {
        offsetY += change / scale;
        if (offsetY > offsetMinY)
            offsetY = offsetMinY;
        else if (offsetY < offsetMaxY)//offset: -123, offsetMax : -1000
            offsetY = offsetMaxY;

    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public Obstacle[] getObstacles() {
        return obstacles;
    }

    public CustomDrawView getCustomDrawView() {
        return cdv;
    }

    private void tickAll(long delayMillis) {
        for (Robot r : robots) {
            r.tick(delayMillis);
        }
    }

    public void startTicking() {
        drawClock.startTicking();
        tickClock.startTicking();
    }

    public void stopTicking() {
        drawClock.stopTicking();
        tickClock.stopTicking();
    }

    public Canvas drawCanvas(@NonNull Canvas c) {

        c.scale(scale, scale);
        c.translate(offsetX, offsetY);
//        Log.v("GameManager", "offset:" + offsetX + "," + offsetY);

        drawBackground(c);
        // draw objects

        for (Robot r : robots) {
            r.draw(c);
        }

        return c;
    }

    private void drawBackground(Canvas c) {
        // draw map background
        c.drawColor(col_b);
      c.drawRect(0, 0, mapWidth, mapHeight, paint_mapBack);
        // draw pre-drawn background bitmap
//        c.drawBitmap(background, 0, 0, null);
    }

    private Bitmap prepareBackground() {
        Bitmap bgBmp = BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.background));
        Bitmap out = Bitmap.createBitmap(mapWidth, mapHeight, Bitmap.Config.ARGB_8888);
        // TODO Konstante für Größe der einzelnen Hintergrundfragmente!!
        int textureWidth = bgBmp.getWidth(), textureHeight = bgBmp.getHeight();
        Canvas c = new Canvas(out);
        for (int x = 0; x < mapWidth; x += textureWidth) {
            for (int y = 0; y < mapHeight; y += textureHeight) {
                Rect r = new Rect(x, y, x + textureHeight, y + textureHeight);
                c.drawBitmap(bgBmp, null, r, paint_mapBack);
            }
        }
        for (Obstacle o : obstacles) {
            o.draw(c);
        }
        return out;
    }

    public boolean isFree(float xW, float yW, Robot i) {
        for (Obstacle o : obstacles) {
            if ((xW + i.getRadius() >= o.x && xW - i.getRadius() <= o.x + o.width) && (yW + i.getRadius() >= o.y && yW - i.getRadius() <= o.y + o.height)) {
                return false;
            }
        }
        for (Robot r : robots) {
            if (r == i)
                continue;
            float dx = xW - r.getX(), dy = yW - r.getY();
            if (Math.sqrt((double) (dx * dx + dy * dy)) <= r.getRadius() + i.getRadius()) {
                return false;
            }
        }
        return true;
    }

    public void setViewSize(int width, int height) {
        screenX = width;
        screenY = height;
        setMaxOffset();
        Log.d("GameManager", "screen (" + screenX + ", " + screenY + "), map (" + mapWidth + ", " + mapHeight + ")");
        cdv.setMinScale(Math.min(1f * screenX / mapWidth, 1f * screenY / mapHeight));
    }

    private void setMaxOffset() {
        offsetMaxX = -(Math.max(mapWidth - Math.round(screenX / scale), 0));
        offsetMaxY = -(Math.max(mapHeight - Math.round(screenY / scale), 0));
    }
}
