
package com.tony.callerloc.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.tony.callerloc.services.CallerlocRetriever;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

/**
 * @author Tony Gao
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
        deleteIfExists();
        combineDatabases();
        createIndexes();
    }

    private void deleteIfExists() {
        
    }

    private void combineDatabases() throws IOException {
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
                    // Log.d(TAG, "count: " + count);
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

    private void createIndexes() {
        Log.d(TAG, "createIndexes called");

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (db != null) {
            try {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_callerlocs_prefix ON "
                        + Constants.CallerLoc.TABLE_CALLERLOC
                        + "(" + Constants.CallerLoc.PREFIX
                        + " ASC)");

                db.execSQL("CREATE INDEX IF NOT EXISTS index_callerlocs_mid ON "
                        + Constants.CallerLoc.TABLE_CALLERLOC
                        + "(" + Constants.CallerLoc.MID
                        + " ASC)");

                db.execSQL("CREATE INDEX IF NOT EXISTS index_fixedline_areacode ON "
                        + Constants.FixedlineAreaCode.TABLE_FIXEDLINE + "("
                        + Constants.FixedlineAreaCode.AREA_CODE + " ASC)");
            } catch (Exception e) {
                Log.e(TAG, "create indexes caught: ", e);
            } finally {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e(TAG, "create indexes, close db caught: ", e);
                }
            }
        } else {
            Log.d(TAG, "create indexes, get db is null");
        }

    }

    public void initSpecialNums() {
        Log.d(TAG, "initSpecialNums called");
        CallerlocRetriever retriever = CallerlocRetriever.getInstance();

        String fileName = "files" + File.separator + "specialNums.txt";
        InputStream is;
        BufferedReader br = null;
        try {
            is = mAssetManager.open(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",");
                retriever.putSpecialNum(arr[0], arr[1]);
                Log.d(TAG, "put special num: " + line);
            }
        } catch (IOException e) {
            Log.e(TAG, "init special numbers caught: ", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                Log.e(TAG, "init special numbers, tring to close bufferedreader, caught: ", e);
            }
            br = null;
            is = null;
        }
    }
}
