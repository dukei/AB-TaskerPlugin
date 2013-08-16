package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.bundle.BundleScrubber;
import com.dukei.android.apps.anybalance.plugins.tasker.bundle.PluginBundleManager;

public final class FireReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         */

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

        if (PluginBundleManager.isBundleValid(bundle))
        {
            final Long accountId = bundle.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Constants.INTENT);
            sendIntent.setData(Uri.fromParts(Constants.DATA_URI_SCHEME, Constants.DATA_URI_SSP+"/"+Long.toString(accountId),null));
            context.sendBroadcast(sendIntent);
        }
    }
}