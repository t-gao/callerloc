
package com.tony.callerloc.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tony.callerloc.R;
import com.tony.callerloc.db.DatabaseInitializer;
import com.tony.callerloc.services.CallerlocRetriever;

/**
 * @author Tony Gao
 */
public class ConfigActivity extends BaseActivity {

    private static final String TAG = "ConfigActivity";

    private static final int DIALOG_PROGRESS_INIT_DB = 0;
    private static final int DIALOG_PROGRESS_UPDATE_CALL_LOGS = 1;
    private static final int DIALOG_ALERT_LOC_INFO = 2;
    private static final int DIALOG_ALERT_UPDATED_CALL_LOGS_NUM = 3;
    private static final int DIALOG_ABOUT = 4;

    private static final String CALL_LOGS_UPDATE_SELECTION = CallLog.Calls._ID + "=?";

    private static final String[] CALL_LOGS_QUERY_PROJECTION = {
            CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME
    };

    private static final int IDX_ID = 0;
    private static final int IDX_NUMBER = 1;
    private static final int IDX_NAME = 2;

    private View mAboutView;
    private TextView mQueryTextView;
    private LinearLayout mQueryInputLayout;
    private EditText mQueryInputEditText;
    private ToggleButton mEnableInBtn;
    private ToggleButton mEnableOutBtn;
    private CheckBox mUpdateCalllogCheck;
    private Spinner mSelectColorSpinner;
    private String[] mColors;

    // for input and query
    private String mLoc;
    private String mNumber;

    private volatile int mUpdatedCallLogsNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mQueryTextView = (TextView) findViewById(R.id.query_num_textview);
        mQueryInputLayout = (LinearLayout) findViewById(R.id.input_num);
        mQueryInputEditText = (EditText) findViewById(R.id.input_num_eidttext);

        mQueryInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    queryLocation();
                    return true;
                }
                return false;
            }
        });

        mUpdateCalllogCheck = (CheckBox) findViewById(R.id.update_calllogs_check);
        mUpdateCalllogCheck.setChecked(mPrefs.getBoolean(PREFERENCES_KEY_UPDATE_CALL_LOG_ENABLED,
                false));

        mUpdateCalllogCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mUpdateCalllogCheck onCheckedChanged, isChecked: " + isChecked);
                mPrefs.edit().putBoolean(PREFERENCES_KEY_UPDATE_CALL_LOG_ENABLED, isChecked)
                        .commit();
            }
        });

        mEnableInBtn = (ToggleButton) findViewById(R.id.enable_incoming);
        mEnableInBtn.setChecked(mPrefs.getBoolean(PREFERENCES_KEY_INCOMING_ENABLED, false));

        mEnableInBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.edit().putBoolean(PREFERENCES_KEY_INCOMING_ENABLED, isChecked).commit();
            }
        });

        mEnableOutBtn = (ToggleButton) findViewById(R.id.enable_outgoing);
        mEnableOutBtn.setChecked(mPrefs.getBoolean(PREFERENCES_KEY_OUTGOING_ENABLED, false));

        mEnableOutBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.edit().putBoolean(PREFERENCES_KEY_OUTGOING_ENABLED, isChecked).commit();
            }
        });

        mColors = getResources().getStringArray(R.array.array_color_names);
        mSelectColorSpinner = (Spinner) findViewById(R.id.select_text_color_spinner);
        mSelectColorSpinner.setAdapter(new ColorSpinnerAdapter(this, R.layout.spinner_row_layout,
                R.id.color_text, mColors));
        mSelectColorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (LOG_ENABLED) {
                    Log.d(TAG, "spinner position selected: " + position);
                }
                mPrefs.edit().putInt(PREFERENCES_KEY_TEXT_COLOR_POS, position).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int initPos = mPrefs.getInt(PREFERENCES_KEY_TEXT_COLOR_POS, BaseActivity.DEFAULT_COLOR_POS);
        if (LOG_ENABLED) {
            Log.d(TAG, "spinner initial position: " + initPos);
        }
        if (initPos > 11) {
            initPos = BaseActivity.DEFAULT_COLOR_POS;
        }
        mSelectColorSpinner.setSelection(initPos);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // init db
        if (!mPrefs.getBoolean(BaseActivity.PREFERENCES_KEY_DB_INITIALIZED, false)) {
            showInitDbProgressDialog();
            new InitDbTask().execute();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS_INIT_DB:
            case DIALOG_PROGRESS_UPDATE_CALL_LOGS:
                return createProgressDialog();
            case DIALOG_ALERT_LOC_INFO:
            case DIALOG_ALERT_UPDATED_CALL_LOGS_NUM:
                return createAlertDialog();
            case DIALOG_ABOUT:
                if (mAboutView == null) {
                    inflateAboutView();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher).setTitle(R.string.app_name)
                        .setView(mAboutView);
                return builder.create();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case DIALOG_ALERT_LOC_INFO:
                ((AlertDialog) dialog).setTitle(R.string.callerloc);
                ((AlertDialog) dialog).setMessage(mNumber + "\n" + mLoc);
                break;
            case DIALOG_ALERT_UPDATED_CALL_LOGS_NUM:
                ((AlertDialog) dialog).setTitle(R.string.refresh_calllogs);
                ((AlertDialog) dialog).setMessage(getResources().getQuantityString(
                        R.plurals.number_of_calllogs_updated, mUpdatedCallLogsNum,
                        mUpdatedCallLogsNum));
                break;
            case DIALOG_PROGRESS_INIT_DB:
                ((ProgressDialog) dialog).setMessage(getString(R.string.init_db_wait_msg));
                break;
            case DIALOG_PROGRESS_UPDATE_CALL_LOGS:
                ((ProgressDialog) dialog).setMessage(getString(R.string.update_calllogs_wait_msg));
                break;
        }
    }

    private Dialog createAlertDialog() {
        return new AlertDialog.Builder(this).setTitle("").setMessage("").create();
    }

    private Dialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("");
        return dialog;
    }

    private void showInitDbProgressDialog() {
        showDialog(DIALOG_PROGRESS_INIT_DB);
    }

    private void removeInitDbProgressDialog() {
        removeDialog(DIALOG_PROGRESS_INIT_DB);
    }

    private void inflateAboutView() {
        mAboutView = LayoutInflater.from(this).inflate(R.layout.about_dialog, null);
        TextView emailView = (TextView) mAboutView.findViewById(R.id.author_email);
        emailView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onInputNumClicked(View v) {
        mQueryTextView.setVisibility(View.GONE);
        mQueryInputLayout.setVisibility(View.VISIBLE);
        mQueryInputEditText.requestFocus();
        mQueryInputEditText.setText(null);
        showSoftInput();
        mNumber = null;
        mLoc = null;
    }

    public void onQueryClicked(View v) {
        queryLocation();
    }

    public void onUpdateCalllogsCheckClicked(View v) {
        Log.d(TAG, "onUpdateCalllogsCheckClicked");
        showDialog(DIALOG_PROGRESS_UPDATE_CALL_LOGS);
        mUpdatedCallLogsNum = 0;

        // this enabled state is already toggled by the clicked, and so is the prefs
        boolean enabled = mPrefs.getBoolean(PREFERENCES_KEY_UPDATE_CALL_LOG_ENABLED, false);
        new UpdateCallLogsTask().execute(Boolean.valueOf(enabled));
    }

    public void onUpdateCalllogsClicked(View v) {
        Log.d(TAG, "onUpdateCalllogsClicked");
        showDialog(DIALOG_PROGRESS_UPDATE_CALL_LOGS);
        mUpdatedCallLogsNum = 0;
        boolean enabled = mPrefs.getBoolean(PREFERENCES_KEY_UPDATE_CALL_LOG_ENABLED, false);

        // toggle checked state, the onCheckedStatedChangedListener will handle
        // the updating of the prefs
        mUpdateCalllogCheck.setChecked(!enabled);

        new UpdateCallLogsTask().execute(Boolean.valueOf(!enabled));
    }

    public void onAboutClicked(View v) {
        showDialog(DIALOG_ABOUT);
    }

    private void queryLocation() {
        mNumber = mQueryInputEditText.getText().toString();
        if (!TextUtils.isEmpty(mNumber)) {
            CallerlocRetriever retriever = CallerlocRetriever.getInstance();
            if (retriever != null) {
                mLoc = retriever.retrieveCallerLocFromDb(this, mNumber);
                if (TextUtils.isEmpty(mLoc)) {
                    mLoc = getString(R.string.unknown_loc);
                }
                showDialog(DIALOG_ALERT_LOC_INFO);
            }
        }
        mQueryInputLayout.setVisibility(View.GONE);
        mQueryTextView.setVisibility(View.VISIBLE);
        hideSoftInput();
    }

    private void showSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mQueryInputEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mQueryInputEditText.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (mQueryInputLayout != null && mQueryInputLayout.getVisibility() == View.VISIBLE) {
            mQueryInputLayout.setVisibility(View.GONE);
            mQueryTextView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * @param insert true to insert loc info to calllogs; false to erase from
     * @return updated calllogs count
     */
    private int updateCallLogs(boolean insert) {
        Log.d(TAG, "updateCallLogs, insert: " + insert);
        int updatedCallLogsNum = 0;
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(CallLog.Calls.CONTENT_URI, CALL_LOGS_QUERY_PROJECTION, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);

        HashMap<Integer, String> calls = new HashMap<Integer, String>();
        if (c != null) {
            try {
                Log.d(TAG, "cursor count: " + c.getCount());
                while (c.moveToNext()) {
                    int id = (int) c.getLong(IDX_ID);
                    String number = c.getString(IDX_NUMBER);
                    String cachedName = c.getString(IDX_NAME);
                    // String label = c.getString(IDX_LABEL);
                    if (!TextUtils.isEmpty(number)) {
                        if (insert) {
                            if (TextUtils.isEmpty(cachedName)) {
                                calls.put(id, number);
                            }
                        } else if (number.contains("<") && number.contains(">")) {
                            calls.put(id, number);
                        }
                    }
                }
            } finally {
                c.close();
            }
        }

        Log.d(TAG, "valid count: " + calls.size());

        CallerlocRetriever retriever = CallerlocRetriever.getInstance();

        if (retriever != null) {
            Iterator<Integer> i = calls.keySet().iterator();
            while (i.hasNext()) {
                Integer id = i.next();
                String number = calls.get(id);
                updatedCallLogsNum += updateCallLog(retriever, cr, id, number, insert);
            }
        }
        return updatedCallLogsNum;
    }

    /**
     * @param insert true to insert loc info to the calllog; false to erase from
     * @return updated rows
     */
    private int updateCallLog(CallerlocRetriever retriever, ContentResolver cr, int id,
            String number, boolean insert) {
        int updatedRowNum = 0;
        ContentValues values = new ContentValues();
        String updatedNumber = null;
        if (insert) {
            String loc = retriever.retrieveCallerLocFromDb(this, number);
            if (!TextUtils.isEmpty(loc)) {
                StringBuilder sb = new StringBuilder();
                sb.append(loc).append("<").append(number).append(">");
                updatedNumber = sb.toString();
            }
        } else {
            updatedNumber = number.substring(number.indexOf("<") + 1, number.indexOf(">"));
        }

        if (updatedNumber != null) {
            values.put(CallLog.Calls.NUMBER, updatedNumber);
            updatedRowNum = cr.update(CallLog.Calls.CONTENT_URI, values,
                    CALL_LOGS_UPDATE_SELECTION, new String[] {
                        String.valueOf(id)
                    });
        }

//        if (LOG_ENABLED) {
//            Log.d(TAG, "updateCallLog -- id: " + id + ", number: " + number + ", updated: "
//                    + updatedNumber);
//        }

        return updatedRowNum;
    }

    private class ColorSpinnerAdapter extends ArrayAdapter {

        private int textViewId;

        public ColorSpinnerAdapter(Context context, int resource, int textViewResourceId,
                Object[] objects) {
            super(context, resource, textViewResourceId, objects);
            textViewId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            if (v != null) {
                TextView text = (TextView) v.findViewById(textViewId);
                if (text != null) {
                    int colorId = getColorIdByPosition(position);
                    if (colorId != -1) {
                        int colr = getResources().getColor(colorId);
                        text.setTextColor(colr);
                    }
                }
            }
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

    }

    private class InitDbTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            try {
                new DatabaseInitializer(getApplicationContext()).initDataBase();
            } catch (IOException e) {
                Log.e(TAG, "Init database error: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            mPrefs.edit().putBoolean(BaseActivity.PREFERENCES_KEY_DB_INITIALIZED, true).commit();
            Log.d(TAG, "init db finished");
            removeInitDbProgressDialog();
        }
    }

    private class UpdateCallLogsTask extends AsyncTask<Boolean, Object, Integer> {

        @Override
        protected Integer doInBackground(Boolean... params) {
            boolean insert = params[0];
            Log.d(TAG, "UpdateCallLogsTask started, insert:" + insert);
            return updateCallLogs(insert);
        }

        @Override
        protected void onPostExecute(Integer result) {
            removeDialog(DIALOG_PROGRESS_UPDATE_CALL_LOGS);
            mUpdatedCallLogsNum = result;
            Log.d(TAG, "UpdateCallLogsTask ended, updated call logs count: " + result);
            showDialog(DIALOG_ALERT_UPDATED_CALL_LOGS_NUM);
        }
    }
}
