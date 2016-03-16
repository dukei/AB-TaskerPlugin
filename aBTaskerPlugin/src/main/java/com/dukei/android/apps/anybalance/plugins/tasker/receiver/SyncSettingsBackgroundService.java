package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dukei.android.lib.anybalance.AnyBalanceProvider;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

public class SyncSettingsBackgroundService extends AbstractBackgroundService {

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        if (null != intent) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                final SettingsAccountObserver observer = new SettingsAccountObserver(getApplicationContext(),
                        (Intent) extra.getParcelable(PluginBundleManager.BUNDLE_EXTRA_ORIG_INTENT));
                final long accountId = extra.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
                final Uri uri = ContentUris.withAppendedId(AnyBalanceProvider.MetaData.Account.CONTENT_URI,
                        accountId);
                observer.getContext().getContentResolver().registerContentObserver(uri, false, observer);
            }
        }
        return START_STICKY;
    }

}
