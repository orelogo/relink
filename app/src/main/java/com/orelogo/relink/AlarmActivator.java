package com.orelogo.relink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Create an alarm to trigger notification.
 */
public class AlarmActivator extends BroadcastReceiver {

    private static final String TAG = "AlarmActivator";

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
            setAlarm(context);
        }
    }

    /**
     * Create alarm to trigger notification if notification is on, otherwise cancel alarm.
     *
     * @param context
     */
    static void setAlarm(Context context) {

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

            long alarmTime = getAlarmTime(timeScale);
            long repeatTime = Convert.getMillisec(timeScale);

            // set alarm
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC, alarmTime, repeatTime, pendingIntent);
        }
        else {
            alarmManager.cancel(pendingIntent); // cancel alarm
            pendingIntent.cancel(); // cancel pending intent
        }
    }

    /**
     * Get time, in unix time, for the the notification alarm based on the time scale (d, w, or m).
     *
     * @param timeScale d, w, or m
     * @return time of alarm, in unix time
     */
    static long getAlarmTime(String timeScale) {
        Calendar time = Calendar.getInstance(); // time when alarm starts

        int alarmHour = 8; // hour for the alarm; 24 hour clock
        int alarmDay = Calendar.MONDAY; // alarm day if alarm is set weekly

        switch (timeScale) {
            case Convert.DAYS_CHAR: // daily alarm
                if (time.HOUR_OF_DAY >= alarmHour) {
                    time.add(Calendar.DAY_OF_YEAR, 1); // add 1  day
                }
                break;

            case Convert.WEEKS_CHAR: // weekly alarm
                if (time.get(Calendar.DAY_OF_WEEK) < alarmDay) {
                    int dayOffset = time.get(Calendar.DAY_OF_WEEK) - alarmDay;
                    time.add(Calendar.DAY_OF_YEAR, dayOffset);
                }
                else if (time.get(Calendar.DAY_OF_WEEK) > alarmDay) {
                    int dayOffset = 7 - (time.get(Calendar.DAY_OF_WEEK) - alarmDay);
                    time.add(Calendar.DAY_OF_YEAR, dayOffset);
                }
                else { // Calendar.DAY_OF_WEEK == alarmDay
                    if (time.HOUR_OF_DAY >= alarmHour) {
                        time.add(Calendar.DAY_OF_YEAR, 7); // add week
                    }
                }
                break;

            case Convert.MONTHS_CHAR: // monthly alarm
                if (time.get(Calendar.DAY_OF_MONTH) > 1 || time.HOUR_OF_DAY >= alarmHour) {
                    time.set(Calendar.DAY_OF_MONTH, 1);
                    time.add(Calendar.MONTH, 1);
                }
                break;

            default:
                break;
        }

        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.HOUR_OF_DAY, alarmHour);

        Date debugDate = new Date(time.getTimeInMillis()); // for debugging date
        Log.d(TAG, "Alarm time: " + DateFormat.getDateInstance(DateFormat.FULL).format(debugDate) +
            " " + DateFormat.getTimeInstance(DateFormat.FULL).format(debugDate));
        return time.getTimeInMillis();
    }
}
