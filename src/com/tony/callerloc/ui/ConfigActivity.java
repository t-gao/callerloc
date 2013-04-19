
package com.tony.callerloc.ui;

import java.io.IOException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tony.callerloc.CallerlocApp;
import com.tony.callerloc.R;
import com.tony.callerloc.db.DatabaseInitializer;

/**
 * @author Tony Gao
 *
 */
public class ConfigActivity extends BaseActivity {

    private static final String TAG = "ConfigActivity";

    private static final int DIALOG_PROGRESS = 1;

    private ToggleButton mEnableBtn;
    private Spinner mSelectColorSpinner;
    private String[] mColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        if (id == DIALOG_PROGRESS) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage(R.string.init_db_wait_msg);
            return dialog;
        }
        return null;
    }

    private void showProgressDialog() {
        showDialog(DIALOG_PROGRESS);
    }

    private void removeProgressDialog() {
        removeDialog(DIALOG_PROGRESS);
    }

    public void onInputNumClicked(View v) {
        //TODO:
    }

    public void onQueryClicked(View v) {
        //TODO:
    }

    public void onRefreshCalllogsClicked(View v) {
        //TODO:
    }

    public void onAboutClicked(View v) {
        //TODO:
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
