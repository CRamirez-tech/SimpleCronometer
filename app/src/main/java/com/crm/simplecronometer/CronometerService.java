package com.crm.simplecronometer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Chronometer;

import androidx.core.app.NotificationCompat;

public class CronometerService extends Service {

    private Chronometer chronometer;
    private long elapsedTime = 0;
    private final IBinder binder = new LocalBinder();
    private NotificationManager notificationManager;
    private Handler handler;
    private Runnable runnable;
    private boolean isPaused = false;

    public class LocalBinder extends Binder {
        CronometerService getService() {
            return CronometerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
        notificationManager = getSystemService(NotificationManager.class);

    }
    private void updateMessages() {
        handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                if (!isPaused) {
                    updateNotification();
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        stopChronometer();
    }

    public void setChronometer(Chronometer chronometer) {
        this.chronometer = chronometer;
    }

    public void startChronometer() {
        if (chronometer != null) {
            chronometer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            chronometer.start();
            updateMessages();
            isPaused = false;
        }
    }
    public void pauseChronometer() {
        if (chronometer != null) {
            chronometer.stop();
            elapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            isPaused = true;
        }
    }
    public void resumeChronometer() {
        if (chronometer != null) {
            chronometer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            chronometer.start();
            isPaused = false;
        }
    }
    public void stopChronometer() {
        if (chronometer != null) {
            chronometer.stop();
            elapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            isPaused = false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "Canal de Notificacion de Tiempo",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(android.R.drawable.ic_menu_rotate)
                .setContentTitle("Esta es tu cuenta")
                .setContentText(getChronometerText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        return builder.build();
    }
    private void updateNotification() {
        if (notificationManager != null) {
            Notification notification = createNotification();
            notificationManager.notify(1, notification);
        }
    }
    private String getChronometerText() {
        if (chronometer != null) {
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            int seconds = (int) (elapsedMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
        return "";
    }
}

