
package com.tony.callerloc.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
    public static final String EXTRA_CALL_STATE = "extra_call_state";

    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_IN = 1;
    public static final int ACTION_TYPE_OUT = 2;
    public static final int ACTION_TYPE_CONNECTED = 3;
    public static final int ACTION_TYPE_MISSED = 4;
    public static final int ACTION_TYPE_STOP = 5;

    public static final int CUSTOM_CALL_STATE_CALLING = 999;

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
        int callState = intent.getIntExtra(EXTRA_CALL_STATE, -1);

        // TelephonyManager t = (TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE);
        // if (t == null) {
        // return;
        // }
        // int callState = t.getCallState();

        CallerlocApp app = (CallerlocApp) getApplication();

        if (callState == CUSTOM_CALL_STATE_CALLING) {
            showFloatingWindow(ACTION_TYPE_OUT, number);
        } else {
            int cachedState = app.getCurrentCallState();
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "onHandleIntent -- state: " + callState + "; cached state: "
                        + cachedState);
            }

            if (callState == TelephonyManager.CALL_STATE_RINGING) {
                showFloatingWindow(ACTION_TYPE_IN, number);
            } else if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
                // answered and connected
                if (cachedState == TelephonyManager.CALL_STATE_RINGING
                        //TODO: Can't know if the an outgoing call is answered
                        //for now, see the comment in CallReceiver.
                        /*|| cachedState == CUSTOM_CALL_STATE_CALLING*/) {
                    showFloatingWindow(ACTION_TYPE_CONNECTED, null);
                } else {
                    // return to not cache the state
                    return;
                }
            } else if (callState == TelephonyManager.CALL_STATE_IDLE) {
                if (cachedState == TelephonyManager.CALL_STATE_RINGING) {
                    showFloatingWindow(ACTION_TYPE_MISSED, null);
                } else {
                    dismissFloatingWindow();
                }
            }
        }

        app.setCurrentCallState(callState);
    }

    private void showFloatingWindow(int actionType, String number) {
        Log.d(TAG, "showFloatingWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_ACTION_TYPE, actionType);
        if (!TextUtils.isEmpty(number)) {
            i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
        }
        startService(i);
    }

    private void dismissFloatingWindow() {
        Log.d(TAG, "dismissFloatingWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        // stopService(i);
        i.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_STOP);
        startService(i);
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
