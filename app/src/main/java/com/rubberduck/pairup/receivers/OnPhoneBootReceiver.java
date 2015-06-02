package com.rubberduck.pairup.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Receiver to which listens to ON_BOOT_COMPLETED messages.
 * We need to recreate the alarm everytime the phone boots up.
 */
public class OnPhoneBootReceiver extends BroadcastReceiver {
    public static final String TAG = "OnPhoneBootReceiver";

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d(TAG, "onReceive()");

        // Start the alarm again for 4 am
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Create intent & pending intent
        Intent intent = new Intent(context, PickRandomPairReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

    }

}
