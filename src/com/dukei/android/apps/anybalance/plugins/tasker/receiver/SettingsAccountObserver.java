package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import java.util.Timer;
import java.util.TimerTask;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.lib.anybalance.AccountEx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import net.dinglisch.android.tasker.TaskerPlugin;

final class SettingsAccountObserver extends AbstractAccountObserver {

	private final Intent intent;
	private final Timer timer = new Timer();

	private void unregister(){
		getContext().getContentResolver().unregisterContentObserver(this);
	}
	
	public SettingsAccountObserver(Context context, Intent origIntent) {
		super(context);
		intent = origIntent;
        final long timeoutHint = TaskerPlugin.Setting.getHintTimeoutMS(intent.getExtras());
        final long timeout = Constants.SYNC_TIMEOUT;
        timer.schedule((new TimerTask(){
			@Override
			public void run() {
				SettingsAccountObserver.this.unregister();
				TaskerPlugin.Setting.signalFinish(SettingsAccountObserver.this.context,intent, TaskerPlugin.Setting.RESULT_CODE_UNKNOWN, null);
			}
        }), Math.min((timeoutHint==-1)?TaskerPlugin.Setting.REQUESTED_TIMEOUT_MS_MAX:timeoutHint,timeout));
	}
	
	@Override
	public void onRow(AccountEx row){
		timer.cancel();
		unregister();
		final long ID = row.getId();
		if (Constants.IS_LOGGABLE) {
			Log.v(Constants.LOG_TAG, String.format("Got result for ID: %d", ID)); //$NON-NLS-1$
		}
		final Bundle varBundle = getVariables(row);
		
		TaskerPlugin.Setting.signalFinish(context, intent, TaskerPlugin.Setting.RESULT_CODE_OK, varBundle);
	}
}