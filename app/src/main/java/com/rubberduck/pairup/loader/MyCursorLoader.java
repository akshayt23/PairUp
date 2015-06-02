package com.rubberduck.pairup.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.model.ApparelType;

/*
 * Extend the CursorLoader class to be able to fetch data frm our database instead of a
 * content provider
 */
public class MyCursorLoader extends CursorLoader {

    public static final String TAG = "MyCursorLoader";

    private Context context;
    private ApparelType apparelType;

    public MyCursorLoader(Context context, ApparelType apparelType) {
        super(context);
        this.context = context;
        this.apparelType = apparelType;
    }

    @Override
    public Cursor loadInBackground() {
        DBHelper dbHelper = new DBHelper(context);

        // Load the data depending on the requirement
        Cursor cursor;
        if (apparelType == ApparelType.SHIRT)
            cursor = dbHelper.getAllShirtsCursor();
        else if (apparelType == ApparelType.TROUSER)
            cursor = dbHelper.getAllTrousersCursor();
        else
            cursor = dbHelper.getAllFavPairsCursor();

        if (cursor != null) {
            cursor.getCount();
        }

        dbHelper.close();
        return cursor;
    }
};
