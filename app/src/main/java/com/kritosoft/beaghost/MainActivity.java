package com.kritosoft.beaghost;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private GameManager gm;
    private boolean stopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        gm = new GameManager(MainActivity.this.getApplication());

        CustomDrawView cv = gm.getCustomDrawView();
        Log.d("MainActivity", "adding view..");
        addContentView(cv, lp);
        Log.d("MainActivity", "View added");

//        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity","STOP!!");
        stopped = true;
        gm.stopTicking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "resume");
        if (stopped) {
            stopped = false;
            gm.startTicking();
        }
    }
}