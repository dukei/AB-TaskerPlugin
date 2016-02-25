package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import java.util.List;

import net.dinglisch.android.tasker.TaskerPlugin;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.ui.EditActivity;
import com.dukei.android.lib.anybalance.AccountEx;
import com.dukei.android.lib.anybalance.AnyBalanceProvider;
import com.dukei.android.lib.anybalance.Counter;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {

	private AccountObserver observer = null;
	
	@Override public void onCreate(){
		super.onCreate();
		if (observer == null)
		 observer = new AccountObserver(getApplicationContext());
	}
	
	@Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
		super.onStartCommand(intent, flags, startId);
		if(null != intent) {
			Bundle extra = intent.getExtras();
			if(extra != null) {
				long accountId = extra.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
				observer.getContext().getContentResolver().registerContentObserver(ContentUris.withAppendedId(
						AnyBalanceProvider.MetaData.Account.CONTENT_URI,accountId), 
						false, observer);
			}
		}
		return START_STICKY;
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

    private static final class AccountObserver extends ContentObserver {

    	private final Context context;
    	
    	public AccountObserver(Context context){
    		super(null);
    		this.context = context;
    	}
    	
    	public Context getContext() {
    		return context;
    	}
    	
    	
    	@Override 
    	public void onChange(boolean selfChange){
    		this.onChange(selfChange,null);
    	}
    	
    	@Override
    	public void onChange(boolean selfChange, Uri uri){
    		if (Constants.IS_LOGGABLE) {
    			Log.v(Constants.LOG_TAG, String.format("Uri changed: %s", uri.toString())); //$NON-NLS-1$
    		}
    		final Intent requeryIntent =
    				new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY).putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
    						EditActivity.class.getName());

    		AccountEx row = AnyBalanceProvider.getAccountEx(getContext(), ContentUris.parseId(uri));
    		
    		final Bundle passthroughBundle = new Bundle();
    		// if got valid row - making list of variables for Tasker
    		if(row.getId() > -1) {
    			final Bundle varBundle = new Bundle();
    			varBundle.putString(Constants.TASKER_VAR_ACCID, Long.toString(row.getId()));

    			int cntIdx = 0;
    			List<Counter> valList = row.getCounters();
    			if(valList != null && !row.isError())  // only if last update was successful 
    				for(Counter val: valList) {
    					if(!val.isInactive())
    						varBundle.putString(Constants.TASKER_VAR_PREFIX+Integer.toString(cntIdx), val.getValueNoUnits());
    					cntIdx++;
    				}		
    			// putting variables into passthrough bundle - will be used in QueryReceiver on REQUEST_QUERY Intent             
    			passthroughBundle.putLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID, row.getId());
    			passthroughBundle.putBundle(PluginBundleManager.BUNDLE_VAR_VALUES, varBundle);
    		}
    		// add passthrough data for intent
    		TaskerPlugin.Event.addPassThroughMessageID(requeryIntent);
    		TaskerPlugin.Event.addPassThroughData(requeryIntent, passthroughBundle);
    		context.sendBroadcast(requeryIntent);
    	}
    }

}
