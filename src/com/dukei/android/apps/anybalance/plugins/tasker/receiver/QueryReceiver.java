/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Locale;

import net.dinglisch.android.tasker.TaskerPlugin;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.ui.EditActivity;
import com.dukei.android.lib.anybalance.bundle.BundleScrubber;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

/**
 * This is the "query" BroadcastReceiver for a Locale Plug-in condition.
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class QueryReceiver extends BroadcastReceiver
{

    /**
     * @param context {@inheritDoc}.
     * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION} Intent. This
     *            should always contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was
     *            saved by {@link EditActivity} and later broadcast by Locale.
     */
	
	
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		/*
		 * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
		 */

		if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION.equals(intent.getAction()))
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
			final int messageId = TaskerPlugin.Event.retrievePassThroughMessageID(intent);
			final long accountId = bundle
					.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
			final boolean changesOnly =bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY);

			if (Constants.IS_LOGGABLE)
			{
				Log.v(Constants.LOG_TAG,
						String.format(Locale.US,
								"MessageId is %d", messageId)); //$NON-NLS-1$
			}

			if ( messageId == -1 ) {
				// no Message id received - need to set up Content observer            	
				if (Constants.IS_LOGGABLE)
				{
					Log.v(Constants.LOG_TAG,
							String.format(Locale.US,
									"AccountId is %d", accountId)); //$NON-NLS-1$
				}
	            context.startService(new Intent(context, BackgroundService.class)
	            		                      .putExtra(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID,accountId)
	            		                      .putExtra(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY,changesOnly));

			} else {
				// Message id received - retrieving values and put them into Tasker variables            	
				final Bundle passThrough = TaskerPlugin.Event.retrievePassThroughData(intent);
				long msgAccountId = passThrough.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
				if(accountId == msgAccountId) {
					if (Constants.IS_LOGGABLE)
					{
						Log.v(Constants.LOG_TAG,
								String.format(Locale.US,
										"MsgAccountId is %d", msgAccountId)); //$NON-NLS-1$
					}
					final Bundle varValues = passThrough.getBundle(PluginBundleManager.BUNDLE_VAR_VALUES);
					if(varValues != null &&
							TaskerPlugin.Condition.hostSupportsVariableReturn(intent.getExtras()))
						TaskerPlugin.addVariableBundle(getResultExtras(true), varValues);  
					setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
				}
			}	

		}
	}

}