package com.theriddlebrothers.disruptor;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.Timer;
import java.util.TimerTask;

public class ConfigureActivity extends DefaultActivity {

    private MediaPlayer mPlayer;
    private final String TAG = "ConfigureActivity";
    private SoundMeter meter;
    private final int SAMPLES_TO_TAKE = 10;
    private int numSamplesTaken = 0;
    private Timer timer;
    private double totalThreshold;
    private double avgThreshold;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mPlayer = MediaPlayer.create(ConfigureActivity.this, R.raw.annoy);
        mPlayer.setLooping(true);

        Button startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                startConfiguration();
            }
        });
    }

    // Monitor external mic  to determine if annoying sound should be played/stopped
    private class MonitorDecibelsTask extends TimerTask {
        public void run() {

            double amplitude = meter.getAmplitude();

            numSamplesTaken++;

            if (numSamplesTaken > SAMPLES_TO_TAKE) {
                stopConfiguration();
                return;
            }

            totalThreshold += amplitude;

            avgThreshold = (totalThreshold + amplitude) / numSamplesTaken;
            Log.d(TAG, "SAMPLE: " + amplitude);
            Log.d(TAG, "AVG: " + avgThreshold);
        }
    }

    public void startConfiguration() {
        Log.d(TAG, "Starting configuration...");

        // Update display
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        LinearLayout startPanel = (LinearLayout)findViewById(R.id.configureStartLayout);
        startPanel.setVisibility(View.GONE);

        // Start sound
        mPlayer.start();

        // Monitor volume
        meter = new SoundMeter();
        meter.start();

        // Call first time to avoid first monitored call being 0
        meter.getAmplitude();

        // Monitor sound to determine default threshold
        timer = new Timer();
        timer.scheduleAtFixedRate(new MonitorDecibelsTask(), 500, 500);
    }

    // Stop annoying sound
    public void stopConfiguration() {
        Log.d(TAG, "Stopping configuration...");
        mPlayer.stop();
        mPlayer.release();
        meter.stop();
        timer.cancel();
        Intent intent = new Intent(this, SoundPlayerActivity.class);
        intent.putExtra("defaultThreshold", avgThreshold);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exitMenuItem:
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
