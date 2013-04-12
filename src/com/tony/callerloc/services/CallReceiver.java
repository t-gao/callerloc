
package com.tony.callerloc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tony.callerloc.ui.BaseActivity;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";
    private static final long INCOMING_CALL_DELAY = 1600;// milliseconds

    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(BaseActivity.PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        if (prefs == null) {
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "SharedPreferences not prepared, exit");
            }
            return;
        }

        if (!prefs.getBoolean(BaseActivity.PREFERENCES_KEY_APP_ENABLED, false)) {
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "app disabled, exit");
            }
            return;
        }

        if (intent == null) {
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String state = extras.getString(TelephonyManager.EXTRA_STATE);
        final String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state != null) {
            Intent i = new Intent(context, CallAnswerService.class);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (BaseActivity.LOG_ENABLED) {
                    Log.d(TAG, "incoming call Broadcast received");
                }
                i.putExtra(CallAnswerService.EXTRA_START_DELAY, INCOMING_CALL_DELAY);
                i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
                if (BaseActivity.LOG_ENABLED) {
                    Log.d(TAG, "CallAnswerService started");
                }
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (BaseActivity.LOG_ENABLED) {
                    Log.d(TAG, "call hungup Broadcast received");
                }
            }
            context.startService(i);
        }

        // Handler callActionHandler = new Handler();
        //
        // Runnable runRingingActivity = new Runnable() {
        // @Override
        // public void run() {
        // Intent intent = new Intent(Intent.ACTION_ANSWER);
        // intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);
        // }
        // };
        //
        // String action = intent.getAction();
        // if (action != null && action.equals(Intent.ACTION_NEW_OUTGOING_CALL))
        // {
        // Log.d(TAG, "OUTGOING CALL");
        // } else {
        // Log.d(TAG, "INCOMING CALL");
        // if (!TextUtils.isEmpty(state) &&
        // state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
        // callActionHandler.postDelayed(runRingingActivity,
        // INCOMING_CALL_DELAY);
        // }
        // }
        //
        // if (TextUtils.isEmpty(state) ||
        // !state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
        // callActionHandler.removeCallbacks(runRingingActivity);
        // }
    }
}
