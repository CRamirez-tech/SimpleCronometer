package com.crm.simplecronometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class MainActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private Button btnStart, btnStop, btnPause;
    private CronometerService foregroundService;
    private boolean serviceBound = false;
    private Button btnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chronometer = findViewById(R.id.chronometer);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnPause = findViewById(R.id.btnPause);
        btnResume = findViewById(R.id.btnResume);
        
        btnStart.setOnClickListener(view -> startService());

        btnStop.setOnClickListener(view -> stopService());
        btnPause.setOnClickListener(view -> pauseChronometer());
        btnResume.setOnClickListener(view -> resumeService());
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CronometerService.LocalBinder binder = (CronometerService.LocalBinder) iBinder;
            foregroundService = binder.getService();
            foregroundService.setChronometer(chronometer);
            foregroundService.startChronometer();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };
    private void startService() {
        Intent serviceIntent = new Intent(this, CronometerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);

        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnResume.setEnabled(false);
        btnStop.setEnabled(true);
    }
    private void resumeService() {
        if (serviceBound) {
            foregroundService.resumeChronometer();
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            btnResume.setEnabled(false);
        }
    }
    private void pauseChronometer() {
        if (serviceBound) {
            foregroundService.pauseChronometer();
            btnPause.setEnabled(false);
            btnResume.setEnabled(true);
        }
    }
    private void stopService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        Intent serviceIntent = new Intent(this, CronometerService.class);
        stopService(serviceIntent);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }
}