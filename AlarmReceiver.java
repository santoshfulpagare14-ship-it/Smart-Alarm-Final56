package com.example.smartalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra("label");
        if (label == null) label = "Alarm";

        // Start service for sound
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("label", label);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // Show ring activity
        Intent ringIntent = new Intent(context, AlarmRingActivity.class);
        ringIntent.putExtra("label", label);
        ringIntent.putExtra("alarm_id", intent.getIntExtra("alarm_id", 0));
        ringIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(ringIntent);
    }
}
