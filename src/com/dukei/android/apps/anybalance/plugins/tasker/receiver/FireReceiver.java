package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import net.dinglisch.android.tasker.TaskerPlugin;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.lib.anybalance.bundle.BundleScrubber;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

public final class FireReceiver extends BroadcastReceiver
{

	public static void sendSettingsEvent(final Context context, 
			                             final long accountId){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Constants.INTENT);
        sendIntent.setData(ContentUris.withAppendedId(Constants.INTENT_DATA_URI,accountId));
        if (Constants.IS_LOGGABLE)
        {
            Log.i(Constants.LOG_TAG,
                  String.format(Locale.US, "Sending intent %s with data %s", sendIntent.getAction(), sendIntent.getData().toString())); //$NON-NLS-1$
        }
        context.sendBroadcast(sendIntent);
	}
	
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         */
        if (Constants.IS_LOGGABLE)
        {
            Log.i(Constants.LOG_TAG,
                  String.format(Locale.US, "Received action %s", intent.getAction())); //$NON-NLS-1$
        }

        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format(Locale.US, "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        if (PluginBundleManager.isBundleValid(bundle)) {
        	if(isOrderedBroadcast() &&
        	   bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_SYNC_EXEC)) {
                if (Constants.IS_LOGGABLE)
                {
                    Log.i(Constants.LOG_TAG,
                          String.format(Locale.US, "Got ordered broadcast for settings %s", intent.getAction())); //$NON-NLS-1$
                }
    			final long accountId = bundle
    					.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
	            context.startService(new Intent(context, SyncSettingsBackgroundService.class)
	                      .putExtra(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID,accountId)
	                      .putExtra(PluginBundleManager.BUNDLE_EXTRA_ORIG_INTENT,intent));
	            sendSettingsEvent(context, 
  					  bundle.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID));
  	        	setResultCode( TaskerPlugin.Setting.RESULT_CODE_PENDING);
        	}	
        	else
  	        	sendSettingsEvent(context, 
  					  bundle.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID));
        }
    }
}