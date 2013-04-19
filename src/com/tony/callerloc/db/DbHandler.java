
package com.tony.callerloc.db;

import com.tony.callerloc.services.CallerlocRetriever;
import com.tony.callerloc.ui.BaseActivity;

import com.tony.callerloc.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Tony Gao
 */
public class DbHandler {

    private static final String TAG = "DbHandler";

    private static final String query_mobile_selection = Constants.CallerLoc.PREFIX + "=? AND "
            + Constants.CallerLoc.MID + "=?";

    private static final String query_fixedline_selection = Constants.FixedlineAreaCode.AREA_CODE
            + "=?";

    private static final String[] QUERY_MOBILE_LOC_PROJECTION = {
        Constants.CallerLoc.LOC
    };
    private static final int INDEX_LOC = 0;

    private static final String[] QUERY_FIXEDLINE_LOC_PROJECTION = {
            Constants.FixedlineAreaCode.PROVINCE, Constants.FixedlineAreaCode.CITY
    };
    private static final int INDEX_PRO = 0;
    private static final int INDEX_CITY = 1;

    private Context mContext;
    private DbHelper mDbHelper;

    public DbHandler(Context context) {
        mContext = context;
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

    private Cursor queryMobile(int prefix, int mid) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] args = {
                String.valueOf(prefix), String.valueOf(mid)
        };
        return db.query(Constants.CallerLoc.TABLE_CALLERLOC, QUERY_MOBILE_LOC_PROJECTION,
                query_mobile_selection, args, null, null, null);
    }

    private Cursor queryFixedline(int areacode) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] args = {
            String.valueOf(areacode)
        };
        return db.query(Constants.FixedlineAreaCode.TABLE_FIXEDLINE,
                QUERY_FIXEDLINE_LOC_PROJECTION, query_fixedline_selection, args, null, null, null);
    }

    public String queryLoc(String number, int type) {
        String loc = null;
        if (type == CallerlocRetriever.NUM_TYPE_INVALID) {
            return null;
        }
        if (type == CallerlocRetriever.NUM_TYPE_MOBILE) {
            int prefix = Integer.valueOf(number.substring(0, 3));
            int mid = Integer.valueOf(number.substring(3, 7));
            Cursor c = queryMobile(prefix, mid);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        loc = c.getString(INDEX_LOC);
                    }
                } finally {
                    c.close();
                }
            }
        } else {
            int areacode = -1;
            if (type == CallerlocRetriever.NUM_TYPE_FIXEDLINE) {
                if (number.startsWith("010")) {
                    return mContext.getString(R.string.city_beijing);
                }
            } else if (type == CallerlocRetriever.NUM_TYPE_FIXEDLINE_NO_AREA_CODE) {
                // TODO: read my city areacode from setting
                // areacode = readMyCityAreaCode();
                if (areacode == -1) {
                    // failed to read same city areacode, return "same city"
                    return mContext.getString(R.string.same_city);
                }
            }

            if (number.startsWith("02")) {
                areacode = Integer.valueOf(number.substring(0, 3));
            } else {
                areacode = Integer.valueOf(number.substring(0, 4));
            }

            if (areacode != -1) {
                Cursor c = queryFixedline(areacode);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            String province = c.getString(INDEX_PRO);
                            String city = c.getString(INDEX_CITY);
                            if (TextUtils.isEmpty(province)) {
                                loc = city;
                            } else {
                                loc = province + " - " + city;
                            }
                        }
                    } finally {
                        c.close();
                    }
                }
            }
        }
        return loc;
    }
}
