package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class AbstractBackgroundService extends Service {

    public AbstractBackgroundService() {
        super();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}