package com.example.smartalarm;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AlarmService getService() { return AlarmService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) { return binder; }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String label = intent != null ? intent.getStringExtra("label") : "Alarm";
        startForeground(1, buildNotification(label != null ? label : "Alarm"));
        startSound();
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("alarm_channel", "Alarms", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String label) {
        Intent fullIntent = new Intent(this, AlarmRingActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, fullIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, "alarm_channel")
                .setContentTitle("Alarm Ringing")
                .setContentText(label)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pi, true)
                .setOngoing(true)
                .build();
    }

    private void startSound() {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartAlarm::Wakelock");
            wakeLock.acquire(10*60*1000L);

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,500,500}, 0));
            } else {
                vibrator.vibrate(new long[]{0,500,500}, 0);
            }

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void stopAlarm() {
        try {
            if (mediaPlayer != null) { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer=null; }
            if (vibrator != null) vibrator.cancel();
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        } catch (Exception ignored) {}
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }
}
