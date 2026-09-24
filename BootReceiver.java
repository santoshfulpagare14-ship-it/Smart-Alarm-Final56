package com.example.smartalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
            int h = prefs.getInt("hour", -1);
            int m = prefs.getInt("minute", -1);
            int id = prefs.getInt("id", 0);
            if (h != -1) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(context, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, m);
                cal.set(Calendar.SECOND, 0);
                if (cal.before(Calendar.getInstance())) cal.add(Calendar.DAY_OF_YEAR, 1);
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi), pi);
            }
        }
    }
}
