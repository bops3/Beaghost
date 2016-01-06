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
    private Clock drawClock, tickClock;

    int col_b = Color.GREEN;

    private Obstacle[] obstacles;
    private Stack<Robot> robots = new Stack<>();
    private Context context;
    private CustomDrawView cdv;
    private int mapSizeX, mapSizeY, offsetX, offsetY;
    private float scale;
    private Thread drawThread, tickThread;

    public GameManager(Context context) {
        this.context = context;
        genMapFromFile(R.raw.map1);
        cdv = new CustomDrawView(context);
        cdv.init(this, obstacles, robots);
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

    public void setOffsetX(float change) {
        offsetX += change;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float change) {
        offsetY += change;
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

    private void tickAll(long delayNanos) {

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
}
