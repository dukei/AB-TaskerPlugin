package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

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
				final long accountId = extra.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
				final boolean changesOnly = extra.getBoolean(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY);
				final Uri uri = ContentUris.withAppendedId(AnyBalanceProvider.MetaData.Account.CONTENT_URI,
						                                   accountId);
				observer.setChangesOnly(uri, changesOnly);
				observer.getContext().getContentResolver().registerContentObserver(uri,false, observer);
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
    	private final HashMap<Uri,Bundle> lastVarMap=new HashMap<Uri,Bundle>(); 
    	private final HashMap<Uri,Boolean> changesOnlyMap=new HashMap<Uri,Boolean>(); 

    	private Boolean isValuesChanged(Uri uri, Bundle current){
    		Bundle last = lastVarMap.put(uri, (Bundle)current.clone());
    		if(last == null) // no old values - treat as changed 
    		  return true;
    		if(last.size() != current.size()) // different bundle sizes - treat as changed
    		  return true;
    		if(!last.keySet().containsAll(current.keySet()))
    		  return true;	
    		for(String key: last.keySet())
    			if(!last.getString(key).equals(current.getString(key)))
    				return true;
    		return false;
    	}
    	
    	public AccountObserver(Context context){
    		super(null);
    		this.context = context;
    	}
    	
    	public Context getContext() {
    		return context;
    	}
    	
    	public void setChangesOnly(Uri uri, boolean val) {
    		changesOnlyMap.put(uri, val);
    	}
    	
    	private boolean getChangesOnly(Uri uri) {
    		return changesOnlyMap.containsKey(uri) && changesOnlyMap.get(uri);
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

    		AccountEx row = AnyBalanceProvider.getAccountEx(getContext(), ContentUris.parseId(uri));
    		
    		// if got valid row - making list of variables for Tasker
    		if(row.getId() > -1) {
    			final Bundle varBundle = new Bundle();
    			varBundle.putString(Constants.TASKER_VAR_ACCID, Long.toString(row.getId()));
    			
    			List<Counter> valList = row.getCounters();
    			if(valList != null && !row.isError())  // only if last update was successful 
    				for(Counter val: valList)
    					if(!val.isInactive())
    						varBundle.putString(Constants.TASKER_VAR_PREFIX+val.getKey(), val.getValueNoUnits());
    			
    			if(!getChangesOnly(uri) || isValuesChanged(uri,varBundle)) {
    	    		if (Constants.IS_LOGGABLE) {
    	    			Log.v(Constants.LOG_TAG, String.format("Values changed on URI: %s", uri.toString())); //$NON-NLS-1$
    	    		}
    	    		final Intent requeryIntent =
    	    				new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY).putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
    	    						EditActivity.class.getName());
        			varBundle.putString(Constants.TASKER_VAR_LAST_CHECKED, Long.toString(row.m_lastChecked));
        			varBundle.putString(Constants.TASKER_VAR_LAST_CHECKED_ERROR, Long.toString(row.m_lastCheckedError));
        			if(row.m_lastCheckedError>row.m_lastChecked)
	        			try {
							varBundle.putString(Constants.TASKER_VAR_LAST_ERROR, row.getLastError().getString("message"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	    		if (Constants.IS_LOGGABLE) {
    	    			Log.v(Constants.LOG_TAG, String.format("Times: %d %d", row.m_lastChecked,row.m_lastCheckedError)); //$NON-NLS-1$
    	    		}
    	    		final Bundle passthroughBundle = new Bundle();
	    			passthroughBundle.putLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID, row.getId());
	    			passthroughBundle.putBundle(PluginBundleManager.BUNDLE_VAR_VALUES, varBundle);
	        		// add passthrough data for intent
	        		TaskerPlugin.Event.addPassThroughMessageID(requeryIntent);
	        		TaskerPlugin.Event.addPassThroughData(requeryIntent, passthroughBundle);
	        		context.sendBroadcast(requeryIntent);
    			}
    		}
    	}
    }

}
