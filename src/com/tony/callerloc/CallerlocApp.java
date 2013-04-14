
package com.tony.callerloc;

import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tony.callerloc.db.DatabaseInitializer;
import com.tony.callerloc.ui.BaseActivity;

/**
 * @author Tony Gao
 */
public class CallerlocApp extends Application {

    private static final String TAG = "CallerlocApp";

    private boolean mInitDbInProgress = false;
    private int mTextColorId;
    private int mCurrentCallState = -1;

    @Override
    public void onCreate() {
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "onCreate entered");
        }
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences(BaseActivity.PREFERENCES_NAME, MODE_PRIVATE);

        // Enable app
        prefs.edit().putBoolean(BaseActivity.PREFERENCES_KEY_APP_ENABLED, true).commit();

        int colorPos = prefs.getInt(BaseActivity.PREFERENCES_KEY_TEXT_COLOR_POS,
                BaseActivity.DEFAULT_COLOR_POS);
        if (colorPos > 11) {
            colorPos = BaseActivity.DEFAULT_COLOR_POS;
        }

        synchronized (this) {
            mTextColorId = getResources().getColor(getColorIdByPosition(colorPos));
        }

        TelephonyManager t = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (t != null) {
            synchronized (this) {
                mCurrentCallState = t.getCallState();
            }
        }

        // init db
        // TODO: update db
        if (!prefs.getBoolean(BaseActivity.PREFERENCES_KEY_DB_INITIALIZED, false)) {
            try {
                mInitDbInProgress = true;
                new DatabaseInitializer(getApplicationContext()).initDataBase();
                prefs.edit().putBoolean(BaseActivity.PREFERENCES_KEY_DB_INITIALIZED, true).commit();
            } catch (IOException e) {
                Log.e(TAG, "Init database error: ", e);
            } finally {
                mInitDbInProgress = false;
                Log.d(TAG, "init db finished");
            }
        }
    }

    int getColorIdByPosition(int position) {
        switch (position) {
            case 0:// white
                return R.color.white;
            case 1:// black
                return R.color.black;
            case 2:// red
                return R.color.red;
            case 3:// grey
                return R.color.grey;
            case 4:// sky blue
                return R.color.sky_blue;
            case 5:// grass green
                return R.color.grass_green;
            case 6:// orange
                return R.color.orange;
            case 7:// pink
                return R.color.pink;
            case 8:// brown
                return R.color.brown;
            case 9:// deep blue
                return R.color.deep_blue;
            case 10:// bright green
                return R.color.bright_green;
            case 11:// bright yellow
                return R.color.bright_yellow;
            default:
                break;
        }
        return -1;
    }

    public synchronized int getTextColorId() {
        return mTextColorId;
    }

    public synchronized int getCurrentCallState() {
        return mCurrentCallState;
    }

    public synchronized void setCurrentCallState(int currentCallState) {
        mCurrentCallState = currentCallState;
    }

    public boolean isInitializingDatabase() {
        return mInitDbInProgress;
    }
}
