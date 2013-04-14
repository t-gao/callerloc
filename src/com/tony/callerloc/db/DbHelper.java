
package com.tony.callerloc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Tony Gao
 *
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    private static final String DB_NAME = "callerloc.db";
    private static final int DB_VER = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
        Log.d(TAG, "constructed!");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Db file is copied from asset, no need to create

        // db.execSQL("CREATE TABLE IF NOT EXISTS "
        // + Constants.CallerLoc.TABLE_CALLERLOC + " ("
        // + Constants.CallerLoc._ID + " INTEGER PRIMARY KEY,"
        // + Constants.CallerLoc.PREFIX + " INTEGER,"
        // + Constants.CallerLoc.MID + " INTEGER,"
        // + Constants.CallerLoc.LOC + " TEXT" + ");");
        //
        // db.execSQL("CREATE INDEX IF NOT EXISTS index_caller on "
        // + Constants.CallerLoc.TABLE_CALLERLOC + " ("
        // + Constants.CallerLoc.PREFIX + ")");

        Log.d(TAG, "onCreate called!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //TODO: delete the db and re-copy from asset

        // db.execSQL("DROP TABLE IF EXISTS "
        // + Constants.CallerLoc.TABLE_CALLERLOC);
        // onCreate(db);
    }

}
