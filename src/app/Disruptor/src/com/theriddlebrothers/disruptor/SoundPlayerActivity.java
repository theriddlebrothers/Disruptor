package com.theriddlebrothers.disruptor;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayerActivity extends DefaultActivity {
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private boolean isContinuous = false;
    private final String TAG = "Disruptor";
    private SoundMeter meter;
    private final double DEFAULT_METER_THRESHOLD = 20;
    private final int METER_MULTIPLIER = 10;
    private double currentMeterThreshold;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundplayer);

        Intent currentIntent = getIntent(); // gets the previously created intent
        double defaultThreshold = currentIntent.getDoubleExtra("defaultThreshold", DEFAULT_METER_THRESHOLD)
                                    * METER_MULTIPLIER;
        int defaultThresholdInt = (int)Math.floor(defaultThreshold);

        this.currentMeterThreshold = getThreshold(defaultThresholdInt);

        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mPlayer = MediaPlayer.create(SoundPlayerActivity.this, R.raw.annoy);
        mPlayer.setLooping(true);

        // Monitor volume
        meter = new SoundMeter();
        meter.start();
        new Timer().scheduleAtFixedRate(new MonitorDecibelsTask(), 100, 100);

        // Play sound while holding button down
        final Button button = (Button) findViewById(R.id.playButton);
        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        toggleSound();
                    }

                    case MotionEvent.ACTION_UP: {
                        toggleSound();
                    }
                }

                return false;
            }
        });


        // Set default threshold
        TextView tv = (TextView) findViewById(R.id.thresholdValue);
        tv.setText(Integer.toString(defaultThresholdInt));

        // Setup seek bar min/max and progress changes
        final SeekBar seekBar = (SeekBar) findViewById(R.id.threshold);
        seekBar.setProgress(defaultThresholdInt);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            public void onProgressChanged (SeekBar seekBar,int progress, boolean fromUser)
            {
                double newProgress = getThreshold(progress);

                TextView tv = (TextView) findViewById(R.id.thresholdValue);
                tv.setText(Integer.toString(progress));

                // Prevent an absolute 0 threshold (otherwise it would just keep playing)
                if (newProgress <= 0) newProgress = 0.1;
                SoundPlayerActivity.this.currentMeterThreshold = newProgress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    // Activity lost focus
    public void onStop() {
        super.onPause();
        stopSound();
    }

    // Activity lost focus
    public void onPause() {
        super.onPause();
        stopSound();
    }

    public void onDestroy() {
        super.onDestroy();
        meter.stop();
        mPlayer.stop();
        mPlayer.release();
        meter = null;
        mPlayer = null;
    }

    private double getThreshold(int rounded) {
        return rounded / METER_MULTIPLIER;
    }

    // Monitor external mic  to determine if annoying sound should be played/stopped
    private class MonitorDecibelsTask extends TimerTask {
        public void run() {

            // Don't start/stop if already continuously playing sound
            if (isContinuous) return;

            double amplitude = meter.getAmplitude();

            if (amplitude >= SoundPlayerActivity.this.currentMeterThreshold && !isPlaying) {
                Log.d(TAG, "Threshold reached: " + Double.toString(amplitude));
                playSound();
            } else if(amplitude < SoundPlayerActivity.this.currentMeterThreshold && isPlaying) {
                Log.d(TAG, "Threshold dropped: " + Double.toString(amplitude));
                stopSound();
            }

        }
    }

    // Toggle continuous play
    public void toggleSound() {
        final Button button = (Button) findViewById(R.id.playButton);
        if (isPlaying) {
            isContinuous = false;
            stopSound();
        }
        else {
            isContinuous = true;
            playSound();
        }
    }

    // Play annoying sound
    public void playSound() {
        Log.d(TAG, "Playing sound...");
        mPlayer.start();
        isPlaying = true;
    }

    // Stop annoying sound
    public void stopSound() {
        Log.d(TAG, "Stopping sound...");
        mPlayer.pause();
        isPlaying = false;
    }
}
