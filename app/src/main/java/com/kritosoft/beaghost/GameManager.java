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
    private Stack<Robot> robots = new Stack<>();
    private Context contex;
    private CustomDrawView cdv;
    private int mapSizeX, mapSizeY, offsetX, offsetY;

    public GameManager(Context contex) {
        this.contex = contex;
        genMapFromFile(R.raw.map1);
        cdv = new CustomDrawView(contex);
        cdv.init(this,obstacles,robots);
    }

    private void genMapFromFile(int mapId) {

        InputStreamReader isr = new InputStreamReader(contex.getResources().openRawResource(mapId));
        Scanner reader = new Scanner(isr);
        int anzObstacles, i = 0;

        mapSizeX = Integer.parseInt(reader.next());
        mapSizeY = Integer.parseInt(reader.next());
        Log.d("GameManager", "Map size: " + mapSizeX + ", " + mapSizeY);
        Log.d("GameManager", "test..");
        String anzOStr = reader.next().trim();
        for (int ii = 0; ii < anzOStr.length(); ii++) {
            char c = anzOStr.charAt(ii);
            Log.d("GameManager", "char " + ii + " :" + c + ", " + ((byte) c));
        }
        Log.d("GameManager", "antOStr:" + anzOStr);
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
