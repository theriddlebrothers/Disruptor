package com.theriddlebrothers.disruptor;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundPlayerActivity extends Activity {
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private boolean isContinuous = false;
    private final String TAG = "Disruptor";
    private SoundMeter meter;
    private final int DEFAULT_METER_THRESHOLD = 20;
    private double currentMeterThreshold;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundplayer);

        this.currentMeterThreshold = getThreshold(DEFAULT_METER_THRESHOLD);

        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mPlayer = MediaPlayer.create(SoundPlayerActivity.this, R.raw.annoy);
        mPlayer.setLooping(true);

        // Monitor volume
        meter = new SoundMeter();
        meter.start();
        new Timer().scheduleAtFixedRate(new MonitorDecibelsTask(), 100, 100);

        // Bind events
        final Button button = (Button) findViewById(R.id.playButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                toggleSound();
            }
        });

        // Set default threshold
        TextView tv = (TextView)findViewById(R.id.thresholdValue);
        tv.setText(Integer.toString(DEFAULT_METER_THRESHOLD));

        // Setup seek bar min/max and progress changes
        final SeekBar seekBar = (SeekBar) findViewById(R.id.threshold);
        seekBar.setProgress(DEFAULT_METER_THRESHOLD);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser)
            {
                double newProgress = getThreshold(progress);

                TextView tv = (TextView)findViewById(R.id.thresholdValue);
                tv.setText(Integer.toString(progress));

                // Prevent an absolute 0 threshold (otherwise it would just keep playing)
                if (newProgress <= 0) newProgress = 0.1;
                SoundPlayerActivity.this.currentMeterThreshold = newProgress;
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
            }
        });
    }

    private double getThreshold(int rounded) {
        return rounded / 10;
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
            button.setText("Play");
        }
        else {
            isContinuous = true;
            playSound();
            button.setText("Stop");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
