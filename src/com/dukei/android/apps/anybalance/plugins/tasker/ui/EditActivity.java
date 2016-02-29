package com.dukei.android.apps.anybalance.plugins.tasker.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import net.dinglisch.android.tasker.TaskerPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.R;
import com.dukei.android.apps.anybalance.plugins.tasker.receiver.FireReceiver;
import com.dukei.android.lib.anybalance.AccountEx;
import com.dukei.android.lib.anybalance.AnyBalanceProvider;
import com.dukei.android.lib.anybalance.Counter;
import com.dukei.android.lib.anybalance.bundle.BundleScrubber;
import com.dukei.android.lib.anybalance.bundle.PluginBundleManager;

public final class EditActivity extends AbstractPluginActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private final String ICON_CONTENT_URI="content://com.dukei.android.provider.anybalance.icon/account-icon/";
	
	private class SimpleCursorAdapterWithUri extends SimpleCursorAdapter {

		public SimpleCursorAdapterWithUri(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		@Override 
		public void setViewImage(ImageView v, String value){
			super.setViewImage(v,ICON_CONTENT_URI+value);
		}
		
	}
	

	static final String[] PROJECTION = new String[] { AnyBalanceProvider.MetaData.AccountEx._ID,
			AnyBalanceProvider.MetaData.AccountEx.NAME };

	private SimpleCursorAdapter mAdapter;
	private ListView list = null;
	private CheckBox changesOnly = null;
	private long accountId = -1;
	private boolean changes = false;
	
	protected static String[] arrayMerge (String[] a, String[] b){
		List<String> both = new ArrayList<String>(Arrays.asList(a));
		both.addAll(Arrays.asList(a));
		return both.toArray(new String [both.size()]);
	}
	
	protected boolean isEventIntent(){
		return getIntent().getAction().equals(Constants.TASKER_EVENT_INTENT);
	}
	
	protected boolean isMainIntent(){
		return getIntent().getAction().equals(Intent.ACTION_MAIN);
	}
	
	protected void addRelevantVairables(Long accId, Intent result){
		if (TaskerPlugin.hostSupportsRelevantVariables(getIntent().getExtras())) {
				final Resources res = getResources();
	    		AccountEx row = AnyBalanceProvider.getAccountEx(this, accId);
    			List<Counter> valList = row.getCounters();
    			List<String> nameList = new ArrayList<String>(Arrays.asList(
    					new String []  {
    							Constants.TASKER_VAR_ACCID+"\n"+res.getString(R.string.var_acc_id),
    							Constants.TASKER_VAR_LAST_CHECKED+"\n"+res.getString(R.string.var_last_checked),
    							Constants.TASKER_VAR_LAST_CHECKED_ERROR+"\n"+res.getString(R.string.var_last_checked_error)
    					}));
    			if(valList != null)  // only if last update was successful 
    				for(Counter val: valList)
    					nameList.add(Constants.TASKER_VAR_PREFIX+val.getKey()+"\n"+
    				                  (val.isTariff()?res.getString(R.string.var_tariff):val.getName()));

				TaskerPlugin.addRelevantVariableList( result, 
						   nameList.toArray(new String[nameList.size()]));
			}      
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BundleScrubber.scrub(getIntent());

		final Bundle localeBundle = getIntent().getBundleExtra(
				com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		BundleScrubber.scrub(localeBundle);
		if (null == savedInstanceState) {
			if (PluginBundleManager.isBundleValid(localeBundle)) {
				accountId = localeBundle
						.getLong(PluginBundleManager.BUNDLE_EXTRA_ACCOUNT_ID);
				changes = localeBundle
						.getBoolean(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY);
				Log.i(Constants.LOG_TAG,
						"Account id = " + Long.toString(accountId)); //$NON-NLS-1$
			}
		}

		setContentView(R.layout.main);
		if(isEventIntent()) {
			changesOnly = (CheckBox) findViewById(R.id.changesOnly);
			changesOnly.setVisibility(View.VISIBLE);
			changesOnly.setChecked(changes);
		}
		

		list = (ListView) findViewById(R.id.list);
		list.setEmptyView(findViewById(R.id.empty));

		String[] fromColumns = {/* AnyBalanceProvider.MetaData.Account._ID,*/ AnyBalanceProvider.MetaData.Account.NAME};
//		int[] toViews = {R.id.img, R.id.name }; 
		int[] toViews = {android.R.id.text1}; 

		mAdapter = new SimpleCursorAdapterWithUri(this,
				/*R.layout.list_item*/android.R.layout.simple_list_item_single_choice, null, fromColumns, toViews, 0);
		list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		list.setAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getSupportLoaderManager().initLoader(0, (Bundle) null, this);
	}

	
	@Override
	public void finish() {
		if (!isCanceled()) {
			int pos = list.getCheckedItemPosition();
			final long accId = list.getItemIdAtPosition(pos);

			if (accId != ListView.INVALID_ROW_ID) {
				final Intent resultIntent = new Intent();

				final Bundle resultBundle = PluginBundleManager.generateBundle(
						getApplicationContext(), accId);
				resultIntent.putExtra(
						com.twofortyfouram.locale.Intent.EXTRA_BUNDLE,
						resultBundle);
				Cursor cursor = (Cursor) list.getItemAtPosition(pos);
				String name = cursor.getString(cursor.getColumnIndex(AnyBalanceProvider.MetaData.Account.NAME));
				if( isEventIntent()) {
					if(changesOnly != null) { 
						resultBundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_CHANGES_ONLY, changesOnly.isChecked());
						if(changesOnly.isChecked()) 
							name+=" ("+getResources().getString(R.string.changes_only).toLowerCase(Locale.getDefault())+")";
					}
					addRelevantVairables(accId, resultIntent);
				}	
				final String blurb = generateBlurb(getApplicationContext(), name);
				resultIntent.putExtra(
						com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
						blurb);
				setResult(RESULT_OK, resultIntent);
				if(isMainIntent())
					FireReceiver.sendSettingsEvent(this, accId);
			}
		}

		super.finish();
	}

	/**
	 * @param context
	 *            Application context.
	 * @param message
	 *            The toast message to be displayed by the plug-in. Cannot be
	 *            null.
	 * @return A blurb for the plug-in.
	 */
	static String generateBlurb(final Context context, final String message) {
		final int maxBlurbLength = context.getResources().getInteger(
				R.integer.twofortyfouram_locale_maximum_blurb_length);

		if (message.length() > maxBlurbLength) {
			return message.substring(0, maxBlurbLength);
		}

		return message;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, AnyBalanceProvider.MetaData.AccountEx.CONTENT_URI, PROJECTION,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		int position = 0;
		for (int i = 0; i < mAdapter.getCount(); i++)
			if (accountId == mAdapter.getItemId(i)) {
				position = i;
				Log.i(Constants.LOG_TAG,
						"Found position = " + Integer.toString(position)); //$NON-NLS-1$

			}
		list.setItemChecked(position, true);
		if (mAdapter.getCount() == 0) {
			View empty = list.getEmptyView();
			empty.findViewById(R.id.progress).setVisibility(View.GONE);
			empty.findViewById(R.id.text).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

}