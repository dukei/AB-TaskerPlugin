package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.lib.anybalance.AccountEx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import net.dinglisch.android.tasker.TaskerPlugin;

final class SettingsAccountObserver extends AbstractAccountObserver {

	private final Intent intent;
		
	public SettingsAccountObserver(Context context, Intent origIntent) {
		super(context);
		intent = origIntent;
	}
	
	@Override
	public void onRow(AccountEx row){
		final long ID = row.getId();
		if (Constants.IS_LOGGABLE) {
			Log.v(Constants.LOG_TAG, String.format("Got result for ID: %d", ID)); //$NON-NLS-1$
		}
		getContext().getContentResolver().unregisterContentObserver(this);
		final Bundle varBundle = getVariables(row);
		
		TaskerPlugin.Setting.signalFinish(context, intent, TaskerPlugin.Setting.RESULT_CODE_OK, varBundle);
	}
}