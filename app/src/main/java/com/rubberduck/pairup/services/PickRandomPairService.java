package com.rubberduck.pairup.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rubberduck.pairup.R;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.model.Pair;

/**
 * Service which picks up a random pair from the database and writes the output to the shared
 * preferences file, which the app can then use to display the pair for the day.
 */
public class PickRandomPairService extends IntentService {
    public static final String TAG = "PickRandomPairService";

    public PickRandomPairService() {
        super("PickRandomPairService");
    }

    public PickRandomPairService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DBHelper dbHelper = new DBHelper(this);
        Pair pair = dbHelper.getRandomPair();
        dbHelper.close();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        // Write the pair details to the shared preferences file
        if (pair != null) {
            Log.d(TAG, "New Pair = " + pair.getShirt().getId() + "," + pair.getTrouser().getId());

            editor.putInt(getString(R.string.prefs_shirt_id), pair.getShirt().getId());
            editor.putInt(getString(R.string.prefs_trouser_id), pair.getTrouser().getId());
            editor.putString(getString(R.string.prefs_shirt_path), pair.getShirt().getImagePath());
            editor.putString(getString(R.string.prefs_trouser_path), pair.getTrouser().getImagePath());

            editor.commit();
        }
    }
}
