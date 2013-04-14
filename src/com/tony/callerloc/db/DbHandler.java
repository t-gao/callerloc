
package com.tony.callerloc.db;

import com.tony.callerloc.ui.BaseActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Tony Gao
 */
public class DbHandler {

    private static final String TAG = "DbHandler";

    private static final String query_selection = Constants.CallerLoc.PREFIX + "=? AND "
            + Constants.CallerLoc.MID + "=?";

    private static final String[] QUERY_LOC_PROJECTION = {
        Constants.CallerLoc.LOC
    };
    private static final int INDEX_LOC = 0;

    private DbHelper mDbHelper;

    public DbHandler(Context context) {
        mDbHelper = new DbHelper(context);
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "constructed!");
        }
    }

    public int bulkInsertValues(ContentValues[] valuesArr) {
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "bulkInsertValues called!");
        }

        int count = -1;

        if (valuesArr != null && valuesArr.length > 0) {
            count++;
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();

            for (ContentValues values : valuesArr) {
                if (0 < db.insert(Constants.CallerLoc.TABLE_CALLERLOC, Constants.CallerLoc.LOC,
                        values)) {
                    count++;
                }
            }
        }

        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "bulkInsertValues returning " + count);
        }

        return count;
    }

    private Cursor queryDb(int prefix, int mid) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] args = {
                String.valueOf(prefix), String.valueOf(mid)
        };
        return db.query(Constants.CallerLoc.TABLE_CALLERLOC, QUERY_LOC_PROJECTION, query_selection,
                args, null, null, null);
    }

    public String queryLoc(int prefix, int mid) {
        Cursor c = queryDb(prefix, mid);
        String loc = null;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    loc = c.getString(INDEX_LOC);
                }
            } finally {
                c.close();
            }
        }
        return loc;
    }
}
