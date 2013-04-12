
package com.tony.callerloc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tony.callerloc.R;
import com.tony.callerloc.services.CallAnswerService;

/**
 * @author Tony.Gao
 */
public class CallerlocActivity extends BaseActivity {

    private static final String TAG = "CallerlocActivity";

    public static final String EXTRA_LOCATION = "extra_loc";

    private static final long RESTART_DELAY = 500;

    private static final String baiduSearchUrlBase = "http://www.baidu.com/s?wd=";

    // private static final int MSG_ID_CHECK_TOP_ACTIVITY = 10;

    private TelephonyManager mTm;
    private BroadcastReceiver mReceiver;
    private String mNumber;
    private String mLoc;

    private TextView mNumberView;
    private TextView mLocView;
    private TextView mSearchButton;
    private WebView mWebView;

    private boolean mSearchedFlag = false;

    // private ActivityManager mActivityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (LOG_ENABLED) {
            Log.d(TAG, "onCreate!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.callerloc_layout);
        mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        registerReciever();
        parseNumberAndLoc(getIntent());
        if (TextUtils.isEmpty(mNumber)) {
            exitCleanly();
        }

        mNumberView = (TextView) findViewById(R.id.caller_number);
        mLocView = (TextView) findViewById(R.id.caller_loc);
        int colorPos = mPrefs.getInt(PREFERENCES_KEY_TEXT_COLOR_POS, 0);
        if (colorPos > 11) {
            colorPos = 0;
        }
        int textColor = getResources().getColor(getColorIdByPosition(colorPos));
        mNumberView.setTextColor(textColor);
        mLocView.setTextColor(textColor);
        mNumberView.setText(mNumber);
        mLocView.setText(mLoc);

        mSearchButton = (TextView) findViewById(R.id.baidusearch);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient());

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // mActivityManager = (ActivityManager)
        // getSystemService(Context.ACTIVITY_SERVICE);
        // mHandler.sendEmptyMessageDelayed(MSG_ID_CHECK_TOP_ACTIVITY,
        // RESTART_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReciever();
        if (mTm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            exitCleanly();
        }

        if (LOG_ENABLED) {
            Log.d(TAG, "onResume!");
        }

    }

    @Override
    protected void onPause() {

        if (LOG_ENABLED) {
            Log.d(TAG, "onPause!");
        }

        super.onPause();
        unregisterReceiver();

        if (!isFinishing()) {
            Intent i = new Intent(getApplicationContext(), CallAnswerService.class);
            i.putExtra(CallAnswerService.EXTRA_START_DELAY, RESTART_DELAY);
            startService(i);
        }
    }

    public void onBaiduSearchClicked(View v) {
        if (mWebView.getVisibility() == View.GONE) {
            mSearchButton.setText(R.string.hide_web);
            mWebView.setVisibility(View.VISIBLE);
            if (!mSearchedFlag) {
                mWebView.loadUrl(baiduSearchUrlBase + mNumber);
            }
            mSearchedFlag = true;
        } else {
            mWebView.setVisibility(View.GONE);
            mSearchButton.setText(mSearchedFlag ? R.string.check_search_result
                    : R.string.baidu_search);
        }
    }

    public void goToAnswerOrDecline(View v) {
        finish();
    }

    /**
     * register phone state receiver
     */
    private void registerReciever() {
        if (mReceiver != null)
            return;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                String state = i.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (state == null || !state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    exitCleanly();
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
    }

    /**
     * unregister phone state receiver
     */
    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void exitCleanly() {
        unregisterReceiver();
        moveTaskToBack(true);

        finish();
    }

    private void parseNumberAndLoc(Intent i) {
        if (i != null) {
            mNumber = i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            mLoc = i.getStringExtra(EXTRA_LOCATION);
        }
    }

    @Override
    protected void onDestroy() {
        if (LOG_ENABLED) {
            Log.d(TAG, "onDestroy!");
        }
        // if (mHandler != null) {
        // mHandler.removeMessages(MSG_ID_CHECK_TOP_ACTIVITY);
        // }
        super.onDestroy();
    }

    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    // private Handler mHandler = new Handler() {
    //
    // @Override
    // public void handleMessage(Message msg) {
    // if (msg.what == MSG_ID_CHECK_TOP_ACTIVITY) {
    // List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
    // if (tasks == null || tasks.get(0) == null || tasks.get(0).topActivity ==
    // null) {
    // return;
    // }
    // String topActivityName = tasks.get(0).topActivity.getClassName();
    // if (topActivityName == null
    // || !topActivityName.equals(CallerlocActivity.this.getComponentName()
    // .getClassName())) {
    // // Try to show on top until user dismiss this activity
    // Intent i = new Intent(CallerlocActivity.this, CallerlocActivity.class);
    // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
    // | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    // | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    // startActivity(i);
    // }
    // sendEmptyMessageDelayed(MSG_ID_CHECK_TOP_ACTIVITY, RESTART_DELAY);
    // }
    // }
    // };
}
