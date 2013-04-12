
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

public class FloatingWindowService extends Service {

    public static final String TAG = "FloatingWindowService";

    private View mFloating;
    WindowManager wm = null;
    WindowManager.LayoutParams wmParams = null;

    private int mCallState = -1;
    private String mNumber;
    private String mLoc;

    private TextView mLabelView;
    private TextView mNumberView;
    private TextView mLocView;

    private RetrieveCallerLocTask mRetrieveTask;

    private boolean mAdded = false;

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
        wmParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
        wmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.CENTER;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        wmParams.width = (int) (screenWidth * 0.35);
        wmParams.height = (int) (wmParams.width * 0.56);

        inflateFloatingView();

        wm.addView(mFloating, wmParams);
        mAdded = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        super.onStartCommand(intent, flags, startId);

        getExtraFromIntent(intent);
        if (mCallState != CallAnswerService.CALL_STATE_MISSED && mNumber != null) {
            if (mRetrieveTask == null) {
                mRetrieveTask = new RetrieveCallerLocTask();
            }
            mRetrieveTask.execute(mNumber);
        }
        setTextViews();
        return START_STICKY;
    }

    private void setTextViews() {
        if (mCallState == CallAnswerService.CALL_STATE_MISSED) {
            if (mLabelView != null) {
                mLabelView.setText(R.string.missed_call);
            }
        } else {
            if (mLabelView != null) {
                mLabelView.setText(R.string.incoming_call);
            }
            if (mNumberView != null) {
                mNumberView.setText(mNumber);
            }
            // if (mLocView != null) {
            // mLocView.setText(mLoc);
            // }
        }
    }

    private void getExtraFromIntent(Intent i) {
        if (i != null) {
            mCallState = i.getExtras().getInt(CallAnswerService.EXTRA_CALL_STATE, -1);
            mNumber = i.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            mLoc = i.getExtras().getString(CallAnswerService.EXTRA_LOC);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (mFloating != null && mAdded) {
            wm.removeView(mFloating);
        }

        super.onDestroy();
    }

    private void inflateFloatingView() {
        Log.d(TAG, "inflateFloatingView");

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
                        Log.d(TAG, "action down, x: , y: " + lastX + " " + lastY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = (int) (event.getRawX() - lastX);
                        float dy = (int) (event.getRawY() - lastY);
                        wmParams.x = paramX + (int) dx;
                        wmParams.y = paramY + (int) dy;
                        wm.updateViewLayout(mFloating, wmParams);
                        Log.d(TAG, "action move, dx: , dy: " + dx + " " + dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        dx = event.getRawX() - lastX;
                        dy = event.getRawY() - lastY;
                        Log.d(TAG, "action up, dx: , dy: " + dx + " " + dy);
                        if (mCallState == CallAnswerService.CALL_STATE_MISSED) {
                            // if it's click
                            if (Math.abs(dx) < 3 && Math.abs(dy) < 3 && mFloating != null) {
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
                loc = retriever.retrieveCallerLoc(params[0]);
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
