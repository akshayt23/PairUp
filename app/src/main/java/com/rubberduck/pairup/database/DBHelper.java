package com.rubberduck.pairup.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcelable;
import android.util.Log;

import com.rubberduck.pairup.database.WardrobeContract.*;
import com.rubberduck.pairup.model.Pair;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.model.Trouser;

import java.util.ArrayList;
import java.util.List;

// Helper class to deal with database operations

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "Akshay/DBHelper";

    // Constants related to the database
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wardrobe.db";

    private static final String INTEGER = " INTEGER";
    private static final String INTEGER_PK = " INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String TEXT_TYPE_NOT_NULL_UNIQUE = " TEXT NOT NULL UNIQUE";
    private static final String COMMA_SEP = ", ";
    private static final String FK = " FOREIGN KEY";
    private static final String REF = " REFERENCES ";

    private static final String CREATE_SHIRTS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ShirtEntry.TABLE_NAME + " (" +
                    ShirtEntry._ID + INTEGER_PK + COMMA_SEP +
                    ShirtEntry.IMG_PATH + TEXT_TYPE_NOT_NULL_UNIQUE +
                    " )";

    private static final String CREATE_TROUSERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TrouserEntry.TABLE_NAME + " (" +
                    TrouserEntry._ID + INTEGER_PK + COMMA_SEP +
                    TrouserEntry.IMG_PATH + TEXT_TYPE_NOT_NULL_UNIQUE +
                    " )";

    private static final String CREATE_FAVORITES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + FavoriteEntry.TABLE_NAME + " (" +
                    FavoriteEntry._ID + INTEGER_PK + COMMA_SEP +
                    FavoriteEntry.SHIRT_ID + INTEGER + COMMA_SEP +
                    FavoriteEntry.TROUSER_ID + INTEGER + COMMA_SEP +
                    FK + "(" + FavoriteEntry.SHIRT_ID + ")" + REF +
                    ShirtEntry.TABLE_NAME + "(" + ShirtEntry._ID + ")" + COMMA_SEP +
                    FK + "(" + FavoriteEntry.TROUSER_ID + ")" + REF +
                    TrouserEntry.TABLE_NAME + "(" + TrouserEntry._ID + ")" +
                    ")";

    private static final String DELETE_SHIRTS_TABLE =
            "DROP TABLE IF EXISTS " + ShirtEntry.TABLE_NAME;
    private static final String DELETE_TROUSERS_TABLE =
            "DROP TABLE IF EXISTS " + TrouserEntry.TABLE_NAME;
    private static final String DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create tables if they dont exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHIRTS_TABLE);
        db.execSQL(CREATE_TROUSERS_TABLE);
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_SHIRTS_TABLE);
        db.execSQL(DELETE_TROUSERS_TABLE);
        db.execSQL(DELETE_FAVORITES_TABLE);

        onCreate(db);
    }

    // Add a shirt to the database, return no of columns affected
    public long addShirt(Shirt shirt) {
        Log.d(TAG, "AddShirt()");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ShirtEntry.IMG_PATH, shirt.getImagePath());

        try {
            return db.insertOrThrow(ShirtEntry.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            Log.d(TAG, "Could not insert to database! " + e);
            return -1;
        }

    }

    // Delete a shirt from the database, return no of columns affected
    public int deleteShirt(Shirt shirt) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(ShirtEntry.TABLE_NAME, ShirtEntry._ID + " = ?",
                new String[]{String.valueOf(shirt.getId())});
    }

    // Add a trouser to the database, return no of columns affected
    public long addTrouser(Trouser trouser) {
        Log.d(TAG, "AddTrouser()");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TrouserEntry.IMG_PATH, trouser.getImagePath());

        try {
            return db.insertOrThrow(TrouserEntry.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            Log.d(TAG, "Could not insert to database! " + e);
            return -1;
        }
    }

    // Delete a trouser from the database, return no of columns affected
    public int deleteTrouser(Trouser trouser) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TrouserEntry.TABLE_NAME, TrouserEntry._ID + " = ?",
                new String[]{String.valueOf(trouser.getId())});
    }

    // Get a cursor object containing all the shirts in the database
    public Cursor getAllShirtsCursor() {
        String selectQuery = "SELECT  * FROM " + ShirtEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        return c;
    }

    // Get a cursor object containing all the trousers in the database
    public Cursor getAllTrousersCursor() {
        String selectQuery = "SELECT  * FROM " + TrouserEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        return c;
    }

    // Add a pair to favorites table
    public long addFavPair(Pair pair) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(FavoriteEntry.SHIRT_ID, pair.getShirt().getId());
        cv.put(FavoriteEntry.TROUSER_ID, pair.getTrouser().getId());

        try {
            return db.insertOrThrow(FavoriteEntry.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            Log.d(TAG, "Could not insert to database! " + e);
            return -1;
        }
    }

    // Delete a favorite pair from the database
    public int deleteFavPair(Pair pair) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(FavoriteEntry.TABLE_NAME,
                FavoriteEntry.SHIRT_ID + " = ? AND " + FavoriteEntry.TROUSER_ID + " = ? ",
                new String[]{String.valueOf(pair.getShirt().getId()), String.valueOf(pair.getTrouser().getId())});
    }


    // Get all pairs saved by the user
    public Cursor getAllFavPairsCursor() {
        String selectQuery =
                "SELECT " + FavoriteEntry.TABLE_NAME + ".*" + COMMA_SEP +
                        ShirtEntry.TABLE_NAME + "." + ShirtEntry.IMG_PATH + COMMA_SEP +
                        TrouserEntry.TABLE_NAME + "." + TrouserEntry.IMG_PATH +
                        " FROM " + FavoriteEntry.TABLE_NAME + COMMA_SEP +
                        ShirtEntry.TABLE_NAME + COMMA_SEP + TrouserEntry.TABLE_NAME +
                        " WHERE " + FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.SHIRT_ID + "=" +
                        ShirtEntry.TABLE_NAME + "." + ShirtEntry._ID + " AND " +
                        FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.TROUSER_ID + "=" +
                        TrouserEntry.TABLE_NAME + "." + TrouserEntry._ID;

        Log.d(TAG, "All Fav Pairs Query = " + selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        return c;
    }

    public boolean isPairAFav(Pair pair) {
        String query = "SELECT * FROM " + FavoriteEntry.TABLE_NAME + " WHERE " +
                FavoriteEntry.SHIRT_ID + " = " + pair.getShirt().getId() + " AND " +
                FavoriteEntry.TROUSER_ID + " = " + pair.getTrouser().getId();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }


    // Get a random pair
    public Pair getRandomPair() {
        Shirt shirt = getRandomShirt();
        Trouser trouser = getRandomTrouser();

        if (trouser != null && shirt != null)
            return new Pair(shirt, trouser);
        else {
            return null;
        }
    }

    // Get a random shirt
    private Shirt getRandomShirt() {
        String selectQuery = "SELECT * FROM " + ShirtEntry.TABLE_NAME + " ORDER BY RANDOM() LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Extract shirt details
        if (c.moveToFirst()) {
            int shirtId = c.getInt((c.getColumnIndex(ShirtEntry._ID)));
            String shirtPath = c.getString(c.getColumnIndex(ShirtEntry.IMG_PATH));

            c.close();
            return new Shirt(shirtId, shirtPath);
        } else {
            return null;
        }
    }

    // Get a random trouser
    private Trouser getRandomTrouser() {
        String selectQuery = "SELECT * FROM " + TrouserEntry.TABLE_NAME + " ORDER BY RANDOM() LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Extract shirt details
        if (c.moveToFirst()) {
            int trouserId = c.getInt((c.getColumnIndex(TrouserEntry._ID)));
            String trouserPath = c.getString(c.getColumnIndex(TrouserEntry.IMG_PATH));

            c.close();
            return new Trouser(trouserId, trouserPath);
        } else {
            return null;
        }
    }

}
