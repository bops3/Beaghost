package com.kritosoft.beaghost;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private GameManager gm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        gm = new GameManager(MainActivity.this.getApplication());

        CustomDrawView cv = gm.getCustomDrawView();
        addContentView(cv, lp);

        gm.startTicking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gm.stopTicking();
    }
}