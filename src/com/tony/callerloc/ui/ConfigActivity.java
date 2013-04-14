
package com.tony.callerloc.ui;

import android.content.Context;
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

/**
 * @author Tony Gao
 *
 */
public class ConfigActivity extends BaseActivity {

    private static final String TAG = "ConfigActivity";
    private ToggleButton mEnableBtn;
    private Spinner mSelectColorSpinner;
    private String[] mColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOG_ENABLED) {
            CallerlocApp app = (CallerlocApp) getApplication();
            Log.d(TAG, "onCreate entered, initdb in progress? " + app.isInitializingDatabase());
        }
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
}
