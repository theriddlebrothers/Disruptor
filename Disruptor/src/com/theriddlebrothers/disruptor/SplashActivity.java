package com.theriddlebrothers.disruptor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();

        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // make sure we close the splash screen so the user won't come back when it presses back key
                finish();

                // start the home screen
                Intent intent = new Intent(SplashActivity.this, ConfigureActivity.class);
                SplashActivity.this.startActivity(intent);
            }

        }, 2000); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called

    }
}
