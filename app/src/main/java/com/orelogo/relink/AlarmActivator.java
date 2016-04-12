package com.orelogo.relink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Create alarm to trigger notification.
 */
public class AlarmActivator extends BroadcastReceiver {

    // PendingIntent ID for triggering NotificationPublisher
    private static final int NOTIFICATION_ALARM = 0;

    /**
     * Creates alarm when device reboots.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            createAlarm(context);
        }
    }

    /**
     * Create alarm to trigger notification.
     *
     * @param context
     */
    static void createAlarm(Context context) {

        // intent to start NotificationPublisher
        Intent intent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, NOTIFICATION_ALARM, intent, 0);

        Calendar calendar = Calendar.getInstance(); // time when alarm starts

        // set alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC, calendar.getTimeInMillis() + 3_000, 10_000, pendingIntent);
    }
}
