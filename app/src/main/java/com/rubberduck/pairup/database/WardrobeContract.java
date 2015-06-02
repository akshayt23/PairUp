package com.rubberduck.pairup.database;

import android.provider.BaseColumns;

// Class defining the database schema

public class WardrobeContract {

    public static final class ShirtEntry implements BaseColumns {

        public static final String TABLE_NAME = "shirts";
        public static final String _ID = "_id";
        public static final String IMG_PATH = "s_path";
    }

    public static final class TrouserEntry implements BaseColumns {

        public static final String TABLE_NAME = "trousers";
        public static final String _ID = "_id";
        public static final String IMG_PATH = "t_path";
    }

    public static final class FavoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";
        public static final String _ID = "_id";
        public static final String SHIRT_ID = "shirt_id";
        public static final String TROUSER_ID = "trouser_id";
    }

}
