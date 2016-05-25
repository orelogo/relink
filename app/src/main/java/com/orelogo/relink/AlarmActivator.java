package com.orelogo.relink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Create an alarm to trigger notification.
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
                context, NOTIFICATION_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isNotifyOn = preferences.getBoolean(SettingsFragment.NOTIFY, true);

        if (isNotifyOn) {
            // get calendar and repetition time based on notify interval preferences
            String timeScale = preferences.getString(SettingsFragment.NOTIFY_INTERVAL,
                    context.getResources().getString(R.string.week_default));

            Calendar calendar = getAlarmCalendar(timeScale);
            long repeatTime = Convert.getMillisec(timeScale);

            // set alarm
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC, calendar.getTimeInMillis(), repeatTime, pendingIntent);
        }
        else {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Get calendar for the the notification alarm based on the time scale (d, w, m, or y).
     *
     * @param timeScale d, w, m, or y
     * @return calendar for alarm
     */
    static Calendar getAlarmCalendar(String timeScale) {
        Calendar calendar = Calendar.getInstance(); // time when alarm starts

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 8);
        calendar.set(Calendar.AM_PM, Calendar.AM);

        if (timeScale == Convert.WEEKS_CHAR) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        if (timeScale == Convert.MONTHS_CHAR) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        }
        if (timeScale == Convert.YEARS_CHAR) {
            calendar.set(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar;
    }
}
