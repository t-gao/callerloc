package com.tony.callerloc.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdateCallLogService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        return START_NOT_STICKY;
    }

}
