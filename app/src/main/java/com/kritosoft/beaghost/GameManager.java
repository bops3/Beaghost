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

    public static final byte TYPE_OBSTACLE = 0, TYPE_ROBOT = 1;
    public static final String DELIM = " ";

    private Obstacle[] obstacles;
    private Stack<Robot> robots;
    private Context contex;
    private CustomDrawView cdv;
    private int mapSizeX, mapSizeY, zoomVersatzX, zoomVersatzY;

    public GameManager(Context contex) {
        this.contex = contex;
        genMapFromFile(R.raw.map1);
        cdv = new CustomDrawView(contex);
        cdv.setGameManager(this);
        cdv.setObstacles(obstacles);
        cdv.setRobots(robots);
    }

    private void genMapFromFile(int mapId) {

        InputStreamReader isr = new InputStreamReader(contex.getResources().openRawResource(mapId));
        Scanner reader = new Scanner(isr).useDelimiter(DELIM);
        int anzObstacles, i = 0;

        mapSizeX = Integer.parseInt(reader.next());
        mapSizeY = Integer.parseInt(reader.next());
        Log.d("GameManager", "Map size: " + mapSizeX + ", " + mapSizeY);
        String anzOStr = reader.next();
        Log.d("GameManager", "antOStr:" + anzOStr);
        anzObstacles = Integer.parseInt(anzOStr);
//        anzObstacles = reader.nextInt();

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

    public int getZoomVersatzX() {
        return zoomVersatzX;
    }

    public void setZoomVersatzX(float change) {
        zoomVersatzX += change;
    }

    public int getZoomVersatzY() {
        return zoomVersatzY;
    }

    public void setZoomVersatzY(float change) {
        zoomVersatzY += change;
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
