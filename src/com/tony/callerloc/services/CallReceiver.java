
package com.tony.callerloc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tony.callerloc.ui.BaseActivity;

/**
 * @author Tony Gao
 *
 */
public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";

    // private static final long INCOMING_CALL_DELAY = 1600;// milliseconds

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(BaseActivity.PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        if (prefs == null) {
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "SharedPreferences not prepared, exit");
            }
            return;
        }

        if (!prefs.getBoolean(BaseActivity.PREFERENCES_KEY_DB_INITIALIZED, false)) {
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "database not ready, exiting!");
            }
            return;
        }

        boolean inEnabled = prefs.getBoolean(BaseActivity.PREFERENCES_KEY_INCOMING_ENABLED, false);
        boolean outEnabled = prefs.getBoolean(BaseActivity.PREFERENCES_KEY_OUTGOING_ENABLED, false);

        // 1
        // FIXME: This doesn't work, action is always
        // android.intent.action.PHONE_STATE not matter incoming or outgoing or
        // idle
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "outgoing call broadcast received, number:" + phoneNumber);
            }
        } else {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            int callState = tm.getCallState();

            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "broadcast received, call state: " + callState);
            }

            Intent i = new Intent(context, CallAnswerService.class);
            switch (callState) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (!inEnabled) {
                        return;
                    }
                    String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!inEnabled && !outEnabled) {
                        return;
                    } else {
                        // do nothing
                    }
                    break;
            }
            context.startService(i);
        }

//        // 2
//        Bundle extras = intent.getExtras();
//        if (extras == null) {
//            return;
//        }
//
//        String state = extras.getString(TelephonyManager.EXTRA_STATE);
//        String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//
//        if (state != null) {
//            if (BaseActivity.LOG_ENABLED) {
//                Log.d(TAG, "broadcast received, call state: " + state + ", number: " + number);
//            }
//
//            Intent i = new Intent(context, CallAnswerService.class);
//            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//
//                if (!prefs.getBoolean(BaseActivity.PREFERENCES_KEY_INCOMING_ENABLED, false)) {
//                    if (BaseActivity.LOG_ENABLED) {
//                        Log.d(TAG, "app disabled, exiting!");
//                    }
//                    return;
//                }
//
//                // i.putExtra(CallAnswerService.EXTRA_START_DELAY,
//                // INCOMING_CALL_DELAY);
//                i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
//
//            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//                // do nothing
//            }
//            context.startService(i);
//        }

    }
}
