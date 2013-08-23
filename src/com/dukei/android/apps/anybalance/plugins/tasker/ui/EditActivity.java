package com.dukei.android.apps.anybalance.plugins.tasker.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;
import com.dukei.android.apps.anybalance.plugins.tasker.MetaData;
import com.dukei.android.apps.anybalance.plugins.tasker.R;
import com.dukei.android.apps.anybalance.plugins.tasker.bundle.BundleScrubber;
import com.dukei.android.apps.anybalance.plugins.tasker.bundle.PluginBundleManager;

public final class EditActivity extends AbstractPluginActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	static final String[] PROJECTION = new String[] { MetaData.Account._ID,
			MetaData.Account.NAME };

	private SimpleCursorAdapter mAdapter;
	private ListView list = null;
	private long accountId = -1;

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
				Log.i(Constants.LOG_TAG,
						"Account id = " + Long.toString(accountId)); //$NON-NLS-1$
			}
		}

		setContentView(R.layout.main);

		list = (ListView) findViewById(R.id.list);
		list.setEmptyView(findViewById(R.id.empty));

		String[] fromColumns = { MetaData.Account.NAME };
		int[] toViews = { android.R.id.text1 }; // The TextView in
												// simple_list_item_1

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_single_choice, null,
				fromColumns, toViews, 0);
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
				String name = cursor.getString(cursor.getColumnIndex(MetaData.Account.NAME));
				final String blurb = generateBlurb(getApplicationContext(), name);
				resultIntent.putExtra(
						com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
						blurb);

				setResult(RESULT_OK, resultIntent);
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
		return new CursorLoader(this, MetaData.Account.CONTENT_URI, PROJECTION,
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