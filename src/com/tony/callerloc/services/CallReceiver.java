
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

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG,
                    "onreceive, action: " + intent.getAction() + "; callState: "
                            + tm.getCallState());
        }

        // Note: An outgoing call fires this receiver twice for two actions:
        // once for "android.intent.action.NEW_OUTGOING_CALL" with call state 0
        // and then once for "android.intent.action.PHONE_STATE" with call state 2
        // TODO: Will there be a third time when this outgoing call is answered?
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            if (!outEnabled) {
                return;
            }
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "outgoing call broadcast received, number:" + phoneNumber
                        + "; callState: " + tm.getCallState());
            }

            Intent i = new Intent(context, CallAnswerService.class);
            i.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
            i.putExtra(CallAnswerService.EXTRA_ACTION_TYPE, CallAnswerService.ACTION_TYPE_OUT);
            context.startService(i);
        } else {
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
                    i.putExtra(Intent.EXTRA_PHONE_NUMBER, number);
                    i.putExtra(CallAnswerService.EXTRA_ACTION_TYPE, CallAnswerService.ACTION_TYPE_IN);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!inEnabled && !outEnabled) {
                        return;
                    }
                    i.putExtra(CallAnswerService.EXTRA_ACTION_TYPE, CallAnswerService.ACTION_TYPE_END);
                    break;
                default:
                    return; // return to not start the service
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
