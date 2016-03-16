package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.lib.anybalance.AccountEx;
import com.dukei.android.lib.anybalance.AnyBalanceProvider;
import com.dukei.android.lib.anybalance.Counter;

import org.json.JSONException;

import java.util.List;

public abstract class AbstractAccountObserver extends ContentObserver {

    protected final Context context;

    public AbstractAccountObserver(Context context) {
        super(null);
        this.context = context;
    }

    public abstract void onRow(AccountEx row);

    protected Bundle getVariables(AccountEx row) {
        final Bundle varBundle = new Bundle();
        if (row.getId() > -1) {
            varBundle.putString(Constants.TASKER_VAR_ACCID, Long.toString(row.getId()));
            varBundle.putString(Constants.TASKER_VAR_LAST_CHECKED, Long.toString(row.m_lastChecked));
            varBundle.putString(Constants.TASKER_VAR_LAST_CHECKED_ERROR, Long.toString(row.m_lastCheckedError));

            if (row.m_lastCheckedError > row.m_lastChecked)
                try {
                    varBundle.putString(Constants.TASKER_VAR_LAST_ERROR, row.getLastError().getString("message"));
                } catch (JSONException e) {
                    if (Constants.IS_LOGGABLE) {
                        Log.e(Constants.LOG_TAG, String.format("Times: %d %d", row.m_lastChecked, row.m_lastCheckedError)); //$NON-NLS-1$
                    }
                }

            List<Counter> valList = row.getCounters();
            if (valList != null && !row.isError())  // only if last update was successful
                for (Counter val : valList)
                    if (!val.isInactive())
                        varBundle.putString(Constants.TASKER_VAR_PREFIX + val.getKey(), val.getValueNoUnits());
        }
        return varBundle;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public final void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public final void onChange(boolean selfChange, Uri uri) {
        if (Constants.IS_LOGGABLE) {
            Log.v(Constants.LOG_TAG, String.format("%s: Uri changed: %s", getClass().getName(), uri.toString())); //$NON-NLS-1$
        }
        AccountEx row = AnyBalanceProvider.getAccountEx(getContext(), ContentUris.parseId(uri));
        if (row.getId() > -1)
            onRow(row);
    }

}