package com.kritosoft.beaghost;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    int col_b = Color.GREEN;
    private Clock drawClock, tickClock;
    private Obstacle[] obstacles;
    private Stack<Robot> robots = new Stack<>();
    private Context context;
    private CustomDrawView cdv;
    //offset and mapsize are measured in the same unit.
    //screenSize unit is different!(depends on scale)
    private int mapSizeX, mapSizeY, offsetX, offsetY, offsetMaxX, offsetMaxY, offsetMinY = 0, offsetMinX = 0, screenX, screenY;
    private float scale;
    private Thread drawThread, tickThread;

    public GameManager(Context context) {
        this.context = context;
        genMapFromFile(R.raw.map1);
        Log.d("GameManager", "creating...");
        cdv = new CustomDrawView(context);
        Log.d("GameManager", "CustomDrawView created!");
        cdv.init(this);
        Log.d("GameManager", "CustomDrawView init finished.");

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

        mapSizeX = Integer.parseInt(reader.next());
        mapSizeY = Integer.parseInt(reader.next());
        Log.d("GameManager", "Map size: " + mapSizeX + ", " + mapSizeY);
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
                    Log.e("GameManager", "Unknown Type: " + type);
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

    public int getMapSizeX() {
        return mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
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

        c.drawColor(col_b);
        synchronized (this) {
            for (Obstacle o : obstacles) {
                o.draw(c);
            }
            for (Robot r : robots) {
                r.draw(c);
            }
        }
        return c;
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
        cdv.setMinScale(Math.min(screenX / mapSizeX, screenY / mapSizeY));
    }

    private void setMaxOffset() {
        offsetMaxX = -(Math.max(mapSizeX - Math.round(screenX / scale), 0));
        offsetMaxY = -(Math.max(mapSizeY - Math.round(screenY / scale), 0));
    }
}
