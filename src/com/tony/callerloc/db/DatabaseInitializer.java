
package com.tony.callerloc.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

/**
 * @author Tony Gao
 *
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";
    private AssetManager mAssetManager;
    private Context mContext;
    private static String DATABASE_PATH;

    public DatabaseInitializer(Context context) {
        mContext = context;
        mAssetManager = context.getResources().getAssets();
        DATABASE_PATH = Environment.getDataDirectory() + File.separator + "data" + File.separator
                + mContext.getPackageName() + File.separator + "databases";
    }

    public void initDataBase() throws IOException {
        Log.d(TAG, "initDataBase called");
        String dbName = DATABASE_PATH + File.separator + "callerloc.db";
        File dir = new File(DATABASE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(dbName);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "init db", e);
        }
        String[] fileNames = mAssetManager.list("dbs");
        for (int i = 1; i <= fileNames.length; i++) {
            String fileName = "dbs" + File.separator + "callerloc" + i + ".db";
            Log.d(TAG, "opening file: " + fileName);
            InputStream is = mAssetManager.open(fileName);
            byte[] buffer = new byte[1024];
            int count = 0;
            try {
                while ((count = is.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                    os.flush();
                    Log.d(TAG, "count: " + count);
                }
            } catch (IOException e) {
                Log.e(TAG, "init db", e);
            }
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
                Log.e(TAG, "init db", e);
            }
        }
        try {
            if (os != null)
                os.close();
        } catch (IOException e) {
            Log.e(TAG, "init db", e);
        }
    }

//    private class InitDbTask extends AsyncTask<Object, Integer, Boolean> {

//        @Override
//        protected Boolean doInBackground(Object... arg0) {
//            return null;
//        }

//        @Override
//        protected void onPostExecute(Boolean result) {
//            super.onPostExecute(result);
//        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//    }
}
