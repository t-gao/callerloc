
package com.tony.callerloc.services;

import com.tony.callerloc.ui.BaseActivity;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Tony Gao
 */
public class UpdateCallLogService extends Service {

    private static final String TAG = "UpdateCallLogService";

    public static final String EXTRA_NUM = "extra_num";
    public static final String EXTRA_LOC = "extra_loc";

    private static final int MSG_UPDATE_CALL_LOG = 1;
    private static final int UPDATE_CALL_LOG_DELAY = 3000;// 3 seconds

    private static final String[] CALL_LOGS_QUERY_PROJECTION = {
            CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME
    };
    private static final int INDX_ID = 0;
    private static final int INDX_NUMBER = 1;
    private static final int INDX_NAME = 2;

    private static final String CALL_LOGS_UPDATE_SELECTION = CallLog.Calls._ID + "=?";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean update = false;
        if (intent != null) {
            String num = intent.getStringExtra(EXTRA_NUM);
            String loc = intent.getStringExtra(EXTRA_LOC);
            Log.d(TAG, "onStartCommand, num: " + num + ", loc: " + loc);
            if (!TextUtils.isEmpty(loc)) {
                Message msg = aHandler.obtainMessage(MSG_UPDATE_CALL_LOG);
                Bundle data = intent.getExtras();
                if (data != null) {
                    msg.setData(data);
                    // Delay, because now the lasted calllog may be not yet in
                    // the db
                    aHandler.sendMessageDelayed(msg, UPDATE_CALL_LOG_DELAY);
                    update = true;
                }
            }
        }

        if (!update) {
            stopMyself();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            aHandler.removeMessages(MSG_UPDATE_CALL_LOG);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy remove handler messages caught: ", e);
        }
        super.onDestroy();
    }

    private void stopMyself() {
        try {
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "stopSelf caught: ", e);
        }
    }

    private int updateCalllog(String num, String loc) {
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "updateCalllog -- num: " + num + ", loc: " + loc);
        }

        if (TextUtils.isEmpty(num)) {
            return 0;
        }

//        String mobile = getString(R.string.op_mb);
//        String unicom = getString(R.string.op_uc);
//        String telecom = getString(R.string.op_tl);
//        String[] operators = new String[]{mobile, unicom, telecom};
//        for (String operator : operators) {
            if (num.indexOf(',') > 0) {
                Log.d(TAG, "contains op");
                num = num.substring(0, num.indexOf(',') - 1);
//                break;
            }
//        }

        ContentResolver cr = getContentResolver();
        int id = 0;
        String number = null;
        Cursor c = cr.query(CallLog.Calls.CONTENT_URI, CALL_LOGS_QUERY_PROJECTION, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        if (c != null) {
            try {
                // get the first(the latest call log)
                if (c.moveToFirst()) {
                    id = (int) c.getLong(INDX_ID);
                    number = c.getString(INDX_NUMBER);
                    String cachedName = c.getString(INDX_NAME);
                    if (BaseActivity.LOG_ENABLED) {
                        Log.d(TAG, "update calllog, queried first -- id: " + id + ", number: "
                                + number);
                    }
                    if (id > 0 && TextUtils.isEmpty(cachedName) && number != null
                            && !number.contains("<") && !number.contains("<")
                            && (number.equals(num) || number.contains(num))) {
                        return updateCallLog(cr, id, number, loc);
                    }
                }
            } finally {
                c.close();
            }
        }

        return 0;
    }

    private int updateCallLog(ContentResolver cr, int id, String number, String loc) {
        int updatedRowNum = 0;
        ContentValues values = new ContentValues();
        String updatedNumber = null;
        StringBuilder sb = new StringBuilder();
        sb.append(loc).append("<").append(number).append(">");
        updatedNumber = sb.toString();

        if (updatedNumber != null) {
            values.put(CallLog.Calls.NUMBER, updatedNumber);
            updatedRowNum = cr.update(CallLog.Calls.CONTENT_URI, values,
                    CALL_LOGS_UPDATE_SELECTION, new String[] {
                        String.valueOf(id)
                    });
        }

        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "updateCallLog -- id: " + id + ", number: " + number + ", updated: "
                    + updatedNumber);
        }

        return updatedRowNum;
    }

    Handler aHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Bundle data = null;
            if (msg.what == MSG_UPDATE_CALL_LOG && ((data = msg.getData()) != null)) {
                String num = data.getString(EXTRA_NUM);
                String loc = data.getString(EXTRA_LOC);
                new UpdateCallLogTask().execute(num, loc);
            } else {
                UpdateCallLogService.this.stopMyself();
            }
        }
    };

    private class UpdateCallLogTask extends AsyncTask<String, Object, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            return updateCalllog(params[0], params[1]);
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "updated calllogs: " + result);
            }
            UpdateCallLogService.this.stopMyself();
        }
    }
}
