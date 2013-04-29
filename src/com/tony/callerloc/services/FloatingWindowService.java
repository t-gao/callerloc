
package com.tony.callerloc.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.tony.callerloc.CallerlocApp;
import com.tony.callerloc.R;
import com.tony.callerloc.ui.BaseActivity;

/**
 * @author Tony Gao
 *
 */
public class FloatingWindowService extends Service {

    public static final String TAG = "FloatingWindowService";

    private View mFloating;
    WindowManager wm = null;
    WindowManager.LayoutParams wmParams = null;

    private int mActionType;
    private String mLastNumber;
    private String mNumber;
    private String mLoc;

    private TextView mLabelView;
    private TextView mNumberView;
    private TextView mLocView;

    private RetrieveCallerLocTask mRetrieveTask;

    // private boolean mAdded = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        wm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        wmParams = new LayoutParams();
        wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.flags |= (LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wmParams.gravity = Gravity.CENTER;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        wmParams.width = (int) (screenWidth * 0.40);
        wmParams.height = (int) (wmParams.width * 0.56);

        inflateFloatingView();

        wm.addView(mFloating, wmParams);
        mFloating.bringToFront();
        // mAdded = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        super.onStartCommand(intent, flags, startId);

        if (mFloating != null) {
            mFloating.bringToFront();
        }

        getExtraFromIntent(intent);
        // TODO: incoming call while in a call?
        if ((mActionType == CallAnswerService.ACTION_TYPE_IN || mActionType == CallAnswerService.ACTION_TYPE_OUT)
                && mNumber != null && !mNumber.equals(mLastNumber)) {
            try {
                if (mRetrieveTask != null && mRetrieveTask.getStatus() != AsyncTask.Status.FINISHED) {
                    mRetrieveTask.cancel(true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception trying to cancel AsyncTask: ", e);
            }
            mRetrieveTask = new RetrieveCallerLocTask();
            mRetrieveTask.execute(mNumber);
        }

        setTextViews();

        if (mActionType == CallAnswerService.ACTION_TYPE_MISSED
                || mActionType == CallAnswerService.ACTION_TYPE_STOP) {
            // Update call log
            Intent i = new Intent(this, UpdateCallLogService.class);
            i.putExtra(UpdateCallLogService.EXTRA_NUM, mNumber);
            i.putExtra(UpdateCallLogService.EXTRA_LOC, mLoc);
            startService(i);

            if (mActionType == CallAnswerService.ACTION_TYPE_STOP) {
                stopSelf();
            }
        }

        return START_STICKY;
    }

    private void setTextViews() {
        if (mActionType == CallAnswerService.ACTION_TYPE_IN
                || mActionType == CallAnswerService.ACTION_TYPE_OUT) {
            if (mNumberView != null) {
                mNumberView.setText(mNumber);
            }
            // if (mLocView != null) {
            // mLocView.setText(mLoc);
            // }
        }

        if (mLabelView != null) {
            int labelStrId = getLabelStringIdByAction(mActionType);
            if (labelStrId > 0) {
                mLabelView.setText(labelStrId);
            }
        }
    }

    private void getExtraFromIntent(Intent i) {
        mLastNumber = mNumber;
        if (i != null) {
            mActionType = i.getExtras().getInt(CallAnswerService.EXTRA_ACTION_TYPE,
                    CallAnswerService.ACTION_TYPE_NONE);
            if (mActionType == CallAnswerService.ACTION_TYPE_IN
                    || mActionType == CallAnswerService.ACTION_TYPE_OUT) {
                mNumber = i.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (mNumber == null) {
                    mNumber = mLastNumber;
                }
            }
            // mLoc = i.getExtras().getString(CallAnswerService.EXTRA_LOC);
        }
    }

    private int getLabelStringIdByAction(int action) {
        switch (action) {
            case CallAnswerService.ACTION_TYPE_IN:
                return R.string.incoming_call;
            case CallAnswerService.ACTION_TYPE_OUT:
                return R.string.outgoing_call;
            case CallAnswerService.ACTION_TYPE_CONNECTED:
                return R.string.connected_call;
            case CallAnswerService.ACTION_TYPE_MISSED:
                return R.string.missed_call;
            case CallAnswerService.ACTION_TYPE_STOP:
                return R.string.ended_call;
            default:
                return 0;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (mFloating != null /* && mAdded */) {
            wm.removeView(mFloating);
        }

        super.onDestroy();
    }

    private void inflateFloatingView() {
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "inflateFloatingView");
        }

        mFloating = LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating, null);

        mLabelView = (TextView) mFloating.findViewById(R.id.label);
        mNumberView = (TextView) mFloating.findViewById(R.id.number);
        mLocView = (TextView) mFloating.findViewById(R.id.loc);
        CallerlocApp app = (CallerlocApp) getApplication();
        int textColor = app.getTextColorId();
        mLabelView.setTextColor(textColor);
        mNumberView.setTextColor(textColor);
        mLocView.setTextColor(textColor);

        mFloating.setOnTouchListener(new OnTouchListener() {

            float lastX, lastY;
            int paramX, paramY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        paramX = wmParams.x;
                        paramY = wmParams.y;

                        if (BaseActivity.LOG_ENABLED) {
                            Log.d(TAG, "action down, x: , y: " + lastX + " " + lastY);
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = (int) (event.getRawX() - lastX);
                        float dy = (int) (event.getRawY() - lastY);
                        wmParams.x = paramX + (int) dx;
                        wmParams.y = paramY + (int) dy;
                        wm.updateViewLayout(mFloating, wmParams);

                        if (BaseActivity.LOG_ENABLED) {
                            Log.d(TAG, "action move, dx: , dy: " + dx + " " + dy);
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        dx = event.getRawX() - lastX;
                        dy = event.getRawY() - lastY;

                        if (BaseActivity.LOG_ENABLED) {
                            Log.d(TAG, "action up, dx: , dy: " + dx + " " + dy);
                        }

                        if (mActionType == CallAnswerService.ACTION_TYPE_MISSED) {
                            // if it's click
                            if (Math.abs(dx) < 2 && Math.abs(dy) < 2 /* && mFloating != null */) {
                                //TODO: goto call history page (optional)
                                stopSelf();
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    private class RetrieveCallerLocTask extends AsyncTask<String, Object, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "RetrieveCallerLocTask#doInBackground");
            String loc = "";
            CallerlocRetriever retriever = CallerlocRetriever.getInstance();
            if (retriever != null) {
                loc = retriever.retrieveCallerLocFromDb(FloatingWindowService.this, params[0], true);
            }
            return loc;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "RetrieveCallerLocTask#onPostExecute");
            mLoc = result;
            if (mLocView != null) {
                mLocView.setText(mLoc);
            }
        }
    }
}
