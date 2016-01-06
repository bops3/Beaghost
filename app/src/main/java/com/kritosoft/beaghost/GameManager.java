package com.kritosoft.beaghost;

import android.content.Context;
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

    private Obstacle[] obstacles;
    private Stack<Robot> robots = new Stack<>();
    private Context context;
    private CustomDrawView cdv;
    private int mapSizeX, mapSizeY, offsetX, offsetY;

    public GameManager(Context context) {
        this.context = context;
        genMapFromFile(R.raw.map1);
        cdv = new CustomDrawView(context);
        cdv.init(this, obstacles, robots);
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


}
