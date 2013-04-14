
package com.tony.callerloc.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tony.callerloc.CallerlocApp;
import com.tony.callerloc.ui.BaseActivity;

/**
 * @author Tony Gao
 *
 */
public class CallAnswerService extends IntentService {

    private static final String TAG = "CallAnswerService";

    public static final String EXTRA_START_DELAY = "extra_start_delay";
    public static final String EXTRA_LOC = "extra_loc";
    public static final String EXTRA_CALL_STATE = "extra_call_state";

    public static final int CALL_STATE_MISSED = 123;

//    Pattern numberPattern = Pattern.compile("[0-9]*");

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

        String number = getNumber(intent);

//        String loc = "";
//        if (isLegalChineseMobileNumber(number)) {
//            loc = CallerlocRetriever.getInstance().retrieveCallerLoc(number);
//        } else {
//            if (BaseActivity.LOG_ENABLED) {
//                Log.d(TAG, "illegal number, won't show loc info");
//            }
//        }

        // long delay = getDelay(intent);
        // if (delay > 0) {
        // try {
        // Thread.sleep(delay);
        // } catch (InterruptedException e) {
        // Log.e(TAG, "Thread sleep exception: ", e);
        // }
        // }

        // make sure the phone is still ringing
        TelephonyManager t = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (t == null) {
            return;
        }
        CallerlocApp app = (CallerlocApp) getApplication();
        int callState = t.getCallState();
        if (callState == TelephonyManager.CALL_STATE_RINGING) {
            // Intent i = new Intent(Intent.ACTION_ANSWER);

            // Intent i = new Intent(context, CallerlocActivity.class);
            // i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
            // i.putExtra(CallerlocActivity.EXTRA_LOCATION, loc);
            // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
            // WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            // | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            // context.startActivity(i);

            showIncomingCallWindow(number/*, loc*/);
        } else if (callState == TelephonyManager.CALL_STATE_IDLE) {
            if (app.getCurrentCallState() == TelephonyManager.CALL_STATE_RINGING) {
                showMissedCallWindow();
            } else {
                dismissFloatingWindow();
            }
        }

        app.setCurrentCallState(callState);
    }

    private long getDelay(Intent i) {
        if (i != null) {
            Bundle extra = i.getExtras();
            if (extra != null) {
                return extra.getLong(EXTRA_START_DELAY);
            }
        }
        return -1;
    }

    private String getNumber(Intent i) {
        String number = null;

        if (i != null) {
            Bundle extra = i.getExtras();
            if (extra != null) {
                number = extra.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            }
        }

        if (number != null) {
            // ONLY supports Chinese mobile numbers
            if (number.startsWith("+86")) {
                number = number.substring(3);
            } else if (number.startsWith("+")) {
                number = number.substring(1);
            }
        }
        return number;
    }

    // private boolean isLegalChineseMobileNumber(String number) {
    // return number != null && number.length() == 11 &&
    // numberPattern.matcher(number).matches();
    // }

    private void showIncomingCallWindow(String number /*, String loc*/) {
        Log.d(TAG, "showIncomingCallWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_CALL_STATE, -1);
        i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number);
//        i.putExtra(EXTRA_LOC, loc);
        startService(i);
    }

    private void showMissedCallWindow() {
        Log.d(TAG, "showMissedCallWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        i.putExtra(EXTRA_CALL_STATE, CALL_STATE_MISSED);
        startService(i);
    }

    private void dismissFloatingWindow() {
        Log.d(TAG, "dismissFloatingWindow");
        Intent i = new Intent(getApplicationContext(), FloatingWindowService.class);
        stopService(i);
    }
}
