
package com.tony.callerloc.ui;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private static final int DIALOG_PROGRESS = 1;
    private static final int DIALOG_LOC_INFO = 2;
    private static final int DIALOG_ABOUT = 3;

    private View mAboutView;
    private TextView mQueryTextView;
    private LinearLayout mQueryInputLayout;
    private EditText mQueryInputEditText;
    private ToggleButton mEnableBtn;
    private Spinner mSelectColorSpinner;
    private String[] mColors;

    // for input and query
    private String mLoc;
    private String mNumber;

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

        mEnableBtn = (ToggleButton) findViewById(R.id.enable);
        mEnableBtn.setChecked(mPrefs.getBoolean(PREFERENCES_KEY_APP_ENABLED, false));

        mEnableBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.edit().putBoolean(PREFERENCES_KEY_APP_ENABLED, isChecked).commit();
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
            showProgressDialog();
            new InitDbTask().execute();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setCancelable(false);
                dialog.setMessage(getString(R.string.init_db_wait_msg));
                return dialog;
            case DIALOG_LOC_INFO:
                return new AlertDialog.Builder(this).setTitle(R.string.callerloc).setMessage("")
                        .create();
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
        if (id == DIALOG_LOC_INFO) {
            ((AlertDialog) dialog).setMessage(mNumber + "\n" + mLoc);
        }
    }

    private void showProgressDialog() {
        showDialog(DIALOG_PROGRESS);
    }

    private void removeProgressDialog() {
        removeDialog(DIALOG_PROGRESS);
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

    public void onRefreshCalllogsClicked(View v) {
        // TODO:
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
                showDialog(DIALOG_LOC_INFO);
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
            removeProgressDialog();
        }
    }
}
