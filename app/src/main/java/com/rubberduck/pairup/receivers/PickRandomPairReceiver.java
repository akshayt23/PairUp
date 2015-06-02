package com.rubberduck.pairup.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rubberduck.pairup.services.PickRandomPairService;

/**
 * Fired everyday at 4 am, used to pick a random pair for the day
 */
public class PickRandomPairReceiver extends BroadcastReceiver {
    public static final String TAG = "PickRandomPairReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");

        // Start the service which will pick a random pair for the day
        Intent serviceIntent = new Intent(context, PickRandomPairService.class);
        context.startService(serviceIntent);
    }
}
