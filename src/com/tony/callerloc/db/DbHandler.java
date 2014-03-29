
package com.tony.callerloc.db;

import com.tony.callerloc.services.CallerlocRetriever;
import com.tony.callerloc.ui.BaseActivity;
import com.tony.callerloc.utils.EncryptUtil;

import com.tony.callerloc.R;
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

    private Cursor queryMobile(int prefix, int mid) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] args = {
                String.valueOf(EncryptUtil.encryptPrefix(prefix)),
                String.valueOf(EncryptUtil.encryptMid(mid))
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

    public String queryLoc(String number, int type, boolean withOperator) {
        String loc = null;
        String specialSuffix = null;
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
            if (withOperator && !TextUtils.isEmpty(loc)) {
                String op = getOpratorByPrefix(prefix);
                if (!TextUtils.isEmpty(op)) {
                    loc += ("\n" + op);
                }
            }
        } else {
            int areacode = -1;
            if (type == CallerlocRetriever.NUM_TYPE_FIXEDLINE) {

                if (number.startsWith("010")) {
                    return mContext.getString(R.string.city_beijing);
                } else if (number.startsWith("02")) {
                    specialSuffix = getSpecial(number.substring(3));
                    areacode = Integer.valueOf(number.substring(0, 3));
                } else if (number.length() > 4) {
                    // may be like 0400xxxxxxx or 0800xxxxxxx
                    if (number.startsWith("0400")) {
                        String special = getSpecial(number.substring(1));
                        if (!TextUtils.isEmpty(special)) {
                            return special;
                        } else {
                            return mContext.getString(R.string.fzz);
                        }
                    } else if (number.startsWith("0800")) {
                        String special = getSpecial(number.substring(1));
                        if (!TextUtils.isEmpty(special)) {
                            return special;
                        } else {
                            return mContext.getString(R.string.ezz);
                        }
                    }
                } else {
                    specialSuffix = getSpecial(number.substring(4));
                    areacode = Integer.valueOf(number.substring(0, 4));
                }
            } else if (type == CallerlocRetriever.NUM_TYPE_FIXEDLINE_NO_AREA_CODE) {
                String special = getSpecial(number);
                if (special != null) {
                    return special;
                }

                if (number.length() > 3) {
                    // may be like 400xxxxxxx or 800xxxxxxx
                    if (number.startsWith("400")) {
                        return mContext.getString(R.string.fzz);
                    } else if (number.startsWith("800")) {
                        return mContext.getString(R.string.ezz);
                    }
                }

                // TODO: read my city areacode from setting
                // areacode = readMyCityAreaCode();
                if (areacode == -1) {
                    // failed to read same city areacode, return "same city"
                    return mContext.getString(R.string.same_city);
                }
            }

            if (areacode != -1) {
                Cursor c = queryFixedline(areacode);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            String province = c.getString(INDEX_PRO);
                            String city = c.getString(INDEX_CITY);
                            if (province == null) {
                                loc = city;
                            } else {
                                loc = province + city;
                            }
                        }
                    } finally {
                        c.close();
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(specialSuffix)) {
            if (TextUtils.isEmpty(loc)) {
                loc = specialSuffix;
            } else {
                loc += ("\n" + specialSuffix);
            }
        }
        return loc;
    }

    private String getSpecial(String key) {
        CallerlocRetriever cr = CallerlocRetriever.getInstance();
        return cr == null ? null : cr.getSpecial(key);
    }

    private String getOpratorByPrefix(int prefix) {
        String op = null;
        switch (Integer.valueOf(prefix).intValue()) {
            case 130:
            case 131:
            case 132:
            case 145:
            case 155:
            case 156:
            case 185:
            case 186:
                op = mContext.getString(R.string.op_uc);
                break;
            case 133:
            case 153:
            case 180:
            case 181:
            case 189:
                op = mContext.getString(R.string.op_tl);
                break;
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 147:
            case 150:
            case 151:
            case 152:
            case 157:
            case 158:
            case 159:
            case 182:
            case 183:
            case 184:
            case 187:
            case 188:
                op = mContext.getString(R.string.op_mb);
                break;
            default:
                break;
        }
        return op;
    }
}
