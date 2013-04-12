
package com.tony.callerloc.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.tony.callerloc.ui.BaseActivity;

public class CallerlocRetriever {

    private static final String TAG = "CallerlocRetriever";

    public static final String ENCODING_GB2312 = "gb2312";

    private static final String callerLocUrlBase = "http://www.youdao.com/smartresult-xml/search.s?jsFlag=true&type=mobile&q=";

    // Returned data is in Json format:
    // updateCall(1, {'product':'mobile','phonenum':'15850781443','location':'½­ËÕ
    // ÄÏ¾©'} , '');

    public static final String JSON_KEY_NUMBER = "phonenum";
    public static final String JSON_KEY_LOCATION = "location";

    private Pattern numberPattern = Pattern.compile("[0-9]*");

    private static volatile CallerlocRetriever mInstance = new CallerlocRetriever();

    public static CallerlocRetriever getInstance() {
        return mInstance;
    }

    private CallerlocRetriever() {

    }

    private boolean isLegalChineseMobileNumber(String number) {
        return number != null && number.length() == 11 && numberPattern.matcher(number).matches();
    }

    /**
     * retrieve the location of the given number.
     * 
     * @param number the phone number to retrieve location
     * @return the location info in the original format returned by
     *         URLConnection
     */
    public String retrieveCallerLoc(String number) {
        //TODO: first try to retrieve from database
        if (!isLegalChineseMobileNumber(number)) {
            return null;
        }
        String result = searchByURLConnection(callerLocUrlBase, number, ENCODING_GB2312);
        if (TextUtils.isEmpty(result) || !result.contains("location")) {
            return null;
        }
        NumLoc numLoc = parseJson(preProcess(result));
        return numLoc.location;
    }

    private String searchByURLConnection(String urlBase, String target, String encoding) {
        String result = null;
        if (!TextUtils.isEmpty(target)) {
            String urlStr = urlBase + target;
            try {
                URL url = new URL(urlStr);
                try {
                    URLConnection connection = url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), encoding));
                    StringBuilder sb = new StringBuilder();
                    try {
                        String str = reader.readLine();
                        while (str != null) {
                            sb.append(str);
                            str = reader.readLine();
                        }
                    } finally {
                        reader.close();
                    }
                    result = sb.toString();
                } catch (IOException e) {
                    if (BaseActivity.LOG_ENABLED) {
                        Log.e(TAG, "", e);
                    }
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, urlStr);
                if (BaseActivity.LOG_ENABLED) {
                    Log.e(TAG, "", e);
                }
            } catch (Exception e) {
                if (BaseActivity.LOG_ENABLED) {
                    Log.e(TAG, "unknown Exception: ", e);
                }
            }
        }
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "String got from internet: " + result);
        }
        return result;
    }

    /**
     * Parse the Json string to a NumLoc object.
     * 
     * @param s the Json string.
     * @return the generated NumLoc object.
     */
    private NumLoc parseJson(String s) {
        NumLoc numLoc = new NumLoc();
        try {
            JSONObject j = new JSONObject(s);
            String number = (String) j.get(JSON_KEY_NUMBER);
            String location = (String) j.get(JSON_KEY_LOCATION);
            numLoc.number = number;
            numLoc.location = location;
        } catch (JSONException e) {
            if (BaseActivity.LOG_ENABLED) {
                Log.d(TAG, "Exception when parse josn string: " + s);
                Log.e(TAG, "JSONException", e);
            }
        } catch (ClassCastException e) {
            if (BaseActivity.LOG_ENABLED) {
                Log.e(TAG, "ClassCastException", e);
            }
        }
        return numLoc;
    }

    private String preProcess(String resultStr) {
        String ret = resultStr;
        if (!TextUtils.isEmpty(ret) && ret.contains("{") && ret.contains("}")) {
            ret = ret.substring(ret.indexOf('{'), ret.lastIndexOf('}') + 1);
        }
        if (BaseActivity.LOG_ENABLED) {
            Log.d(TAG, "String after pre process: " + ret);
        }
        return ret;
    }

    private class NumLoc {
        public String number;
        public String location;

        @Override
        public String toString() {
            return "Num: " + number + "; Loc: " + location;
        }
    }
}
