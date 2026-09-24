package com.example.smartalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmRingActivity extends AppCompatActivity {

    private AlarmService alarmService;
    private boolean bound = false;
    private int snoozeCount = 0;
    private final int MAX_SNOOZE = 3;
    private final int SNOOZE_MIN = 5;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            alarmService = ((AlarmService.LocalBinder) service).getService();
            bound = true;
        }
        @Override public void onServiceDisconnected(ComponentName name) { bound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.activity_alarm_ring);

        TextView timeView = findViewById(R.id.alarmTime);
        TextView labelView = findViewById(R.id.alarmLabel);
        Button dismissBtn = findViewById(R.id.dismissBtn);
        Button snoozeBtn = findViewById(R.id.snoozeBtn);

        String label = getIntent().getStringExtra("label");
        if (label == null) label = "Alarm";
        labelView.setText(label);
        timeView.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));

        dismissBtn.setOnClickListener(v -> dismissAlarm());
        snoozeBtn.setOnClickListener(v -> snoozeAlarm());

        bindService(new Intent(this, AlarmService.class), conn, Context.BIND_AUTO_CREATE);
    }

    private void dismissAlarm() {
        if (alarmService != null) alarmService.stopAlarm();
        Toast.makeText(this, "Alarm Dismissed", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void snoozeAlarm() {
        if (snoozeCount < MAX_SNOOZE) {
            snoozeCount++;
            if (alarmService != null) alarmService.stopAlarm();

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("label", getIntent().getStringExtra("label"));
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            long triggerAt = System.currentTimeMillis() + SNOOZE_MIN * 60 * 1000L;
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);

            Toast.makeText(this, "Snoozed for " + SNOOZE_MIN + " min (" + snoozeCount + "/" + MAX_SNOOZE + ")", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Max snooze reached", Toast.LENGTH_SHORT).show();
            dismissAlarm();
        }
    }

    // ⭐ VOLUME BUTTON LOGIC - MOST IMPORTANT
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            dismissAlarm(); // Volume Down = Dismiss
            return true; // event consume
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            snoozeAlarm(); // Volume Up = Snooze
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) unbindService(conn);
    }
}
