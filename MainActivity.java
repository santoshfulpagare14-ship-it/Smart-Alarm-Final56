package com.example.smartalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.TextView;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView infoText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = findViewById(R.id.infoText);
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        prefs = getSharedPreferences("alarms", MODE_PRIVATE);

        updateInfo();

        fab.setOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            setAlarm(hourOfDay, minute);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    private void setAlarm(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("label", "Smart Alarm");

        int alarmId = (int) System.currentTimeMillis();
        PendingIntent pi = PendingIntent.getBroadcast(this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // For exact alarm on Android 12+
        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pi);
        alarmManager.setAlarmClock(info, pi);

        // Save for boot receiver
        prefs.edit().putInt("hour", hour).putInt("minute", minute).putInt("id", alarmId).apply();

        Toast.makeText(this, "Alarm set for " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
        updateInfo();
    }

    private void updateInfo() {
        int h = prefs.getInt("hour", -1);
        int m = prefs.getInt("minute", -1);
        if (h != -1) {
            infoText.setText("Next Alarm: " + String.format("%02d:%02d", h, m) + "\n\nVolume DOWN = Dismiss\nVolume UP = Snooze 5 min");
        } else {
            infoText.setText("No alarm set\nTap + to add");
        }
    }
}
