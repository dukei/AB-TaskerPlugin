package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import com.dukei.android.lib.anybalance.AnyBalanceProvider;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class EventBackgroundService extends AbstractBackgroundService {

	private static EventAccountObserver observer = null;
	
	@Override public void onCreate(){
		super.onCreate();
		if (observer == null)
		 observer = new EventAccountObserver(getApplicationContext());
	}
	
	@Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
		super.onStartCommand(intent, flags, startId);
		if(null != intent) {
			Bundle extra = intent.getExtras();
			if(extra != null) {
				final long accountId = extra.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
				final boolean changesOnly = extra.getBoolean(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY);
				final Uri uri = ContentUris.withAppendedId(AnyBalanceProvider.MetaData.Account.CONTENT_URI,
						                                   accountId);
				observer.setChangesOnly(accountId, changesOnly);
				observer.getContext().getContentResolver().registerContentObserver(uri,false, observer);
			}
		}
		return START_STICKY;
	}
}
