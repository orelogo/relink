package com.orelogo.relink;



import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

/**
 * Publish notification.
 */
public class NotificationPublisher extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 0; // notification id

    /**
     * Publish notification.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String reminder = remindersDue(context); // reminder message (or null if no reminders due

        if (reminder != null) {
            // build notification
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_person_add_black_36dp)
                            .setContentTitle("Relink Reminder")
                            .setContentText(reminder);

            // set intent for when user clicks on notification
            Intent clickIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(clickIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(pendingIntent);

            // publish  notification
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
        else {
            notificationManager.cancel(NOTIFICATION_ID); // cancel notification if there is one
        }
    }

    /**
     * Builds reminder message for who to reconnect with.
     *
     * @param context
     * @return reminder message, or null
     */
    @Nullable
    private String remindersDue(Context context) {

        long currentTime = System.currentTimeMillis();
        DBAdapter db = new DBAdapter(context);
        db.open();
        Cursor cursor =  db.getRowsBefore(currentTime); // cursor with all rows
        db.close();

        int dueCount = cursor.getCount();

        if (cursor != null && dueCount > 0) { // at least one reminder is due
            String reminder = "Reconnect with ";
            reminder += cursor.getString(DBAdapter.COL_NAME_INDEX);

            if (dueCount > 1) { // more than one reminder is due
                reminder += " and ";
                reminder += dueCount - 1;
                reminder += " others";
            }
            cursor.close();
            return reminder;
        } else { // empty cursor
            return null;
        }

    }

}
