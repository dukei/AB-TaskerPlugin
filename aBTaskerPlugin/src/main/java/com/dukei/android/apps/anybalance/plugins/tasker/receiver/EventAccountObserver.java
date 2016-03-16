package com.dukei.android.apps.anybalance.plugins.tasker.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.ui.EditActivity;
import com.dukei.android.lib.anybalance.AccountEx;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

import net.dinglisch.android.tasker.TaskerPlugin;

import java.util.HashMap;

final class EventAccountObserver extends AbstractAccountObserver {

    private static String[] UNCOMPARABLE_VARS = {
            Constants.TASKER_VAR_LAST_CHECKED,
            Constants.TASKER_VAR_LAST_CHECKED_ERROR
    };


    private final HashMap<Long, Bundle> lastVarMap = new HashMap<Long, Bundle>();
    private final HashMap<Long, Boolean> changesOnlyMap = new HashMap<Long, Boolean>();


    public EventAccountObserver(Context context) {
        super(context);
    }

    private static Bundle removeUncomparableVars(final Bundle bundle) {
        final Bundle clone = (Bundle) bundle.clone();
        for (String key : UNCOMPARABLE_VARS)
            clone.remove(key);
        return clone;
    }

    private Boolean isValuesChanged(Long id, Bundle bundle) {
        final Bundle current = removeUncomparableVars(bundle);
        final Bundle last = lastVarMap.put(id, current);
        if (last == null) // no old values - treat as changed
            return true;
        if (last.size() != current.size()) // different bundle sizes - treat as changed
            return true;
        if (!last.keySet().containsAll(current.keySet()))
            return true;
        for (String key : last.keySet())
            if (!last.getString(key).equals(current.getString(key)))
                return true;
        return false;
    }

    public void setChangesOnly(Long id, boolean val) {
        changesOnlyMap.put(id, val);
    }

    private boolean getChangesOnly(Long id) {
        return changesOnlyMap.containsKey(id) && changesOnlyMap.get(id);
    }

    @Override
    public void onRow(AccountEx row) {
        final Bundle varBundle = getVariables(row);
        final long ID = row.getId();
        if (!getChangesOnly(ID) || isValuesChanged(ID, varBundle)) {
            if (Constants.IS_LOGGABLE) {
                Log.v(Constants.LOG_TAG, String.format("Values changed on ID: %d", ID)); //$NON-NLS-1$
            }
            final Intent requeryIntent =
                    new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY).putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
                            EditActivity.class.getName());
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