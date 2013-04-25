
package com.tony.callerloc.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tony.callerloc.CallerlocApp;
import com.tony.callerloc.ui.BaseActivity;

/**
 * @author Tony Gao
 */
public class CallAnswerService extends IntentService {

    private static final String TAG = "CallAnswerService";

    public static final String EXTRA_START_DELAY = "extra_start_delay";
    public static final String EXTRA_LOC = "extra_loc";
    public static final String EXTRA_ACTION_TYPE = "extra_action_type";

    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_IN = 1;
    public static final int ACTION_TYPE_OUT = 2;
    public static final int ACTION_TYPE_END = 3;

    public CallAnswerService() {
        super("CallAnswerService");
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "constructed");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "onHandleIntent");
        }

        Context context = getBaseContext();
        if (context == null) {
            return;
        }

        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        int actionType = intent.getIntExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_NONE);

        TelephonyManager t = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (t == null) {
            return;
        }
        CallerlocApp app = (CallerlocApp) getApplication();
        int callState = t.getCallState();

        if (actionType == ACTION_TYPE_OUT) {
            showOutgoingCallWindow(number);
        } else {
            int cachedState = app.getCurrentCallState();
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "onHandleIntent -- state: " + callState + "; cached state: " + cachedState);
            }
            
            if (callState == TelephonyManager.CALL_STATE_RINGING) {
                // make sure the phone is still ringing
                showIncomingCallWindow(number);
            } else if (callState == TelephonyManager.CALL_STATE_IDLE) {
                if (cachedState == TelephonyManager.CALL_STATE_RINGING) {
                    showEndedCallWindow();
                } else {
                    dismissFloatingWindow();
                }
            }
        }

        app.setCurrentCallState(callState);
    }

    private void showOutgoingCallWindow(String number) {
        Log.d(TAG, "showOutgoingCallWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_OUT);
        i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
        startService(i);
    }

    private void showIncomingCallWindow(String number) {
        Log.d(TAG, "showIncomingCallWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_IN);
        i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
        startService(i);
    }

    private void showEndedCallWindow() {
        Log.d(TAG, "showMissedCallWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_END);
        startService(i);
    }

    private void dismissFloatingWindow() {
        Log.d(TAG, "dismissFloatingWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        stopService(i);
    }

    // private void showCoveringActivity() {
    // Intent i = new Intent(context, CallerlocActivity.class);
    // i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
    // i.putExtra(CallerlocActivity.EXTRA_LOCATION, loc);
    // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    // WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
    // | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    // context.startActivity(i);
    // }
}
