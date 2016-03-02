package com.dukei.android.lib.anybalance.bundle;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.dukei.android.apps.anybalance.plugins.tasker.Constants;

/**
 * Class for managing the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} for this plug-in.
 */
public final class PluginBundleManager
{
    
	public static final String BUNDLE_CLASS_NAME="com.dukei.android.apps.anybalance.plugins.tasker";
	/**
     * Type: {@code long}.
     * <p>
     * Account ID.
     */
		
    public static final String BUNDLE_EXTRA_ACCOUNT_ID = BUNDLE_CLASS_NAME+".LONG_ACCOUNT_ID"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_CHANGES_ONLY = BUNDLE_CLASS_NAME+".BOOLEAN_CHANGES_ONLY"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_SYNC_EXEC = BUNDLE_CLASS_NAME+".BOOLEAN_SYNC_EXEC"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_ORIG_INTENT = BUNDLE_CLASS_NAME+".INTENT_ORIGINAL_INTENT"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_TIMEOUT = BUNDLE_CLASS_NAME+".LONG_TIMEOUT"; //$NON-NLS-1$
    public static final String BUNDLE_VAR_VALUES = BUNDLE_CLASS_NAME+".BUNDLE_VAR_VALUES"; //$NON-NLS-1$

    /**
     * Type: {@code int}.
     * <p>
     * versionCode of the plug-in that saved the Bundle.
     */
    /*
     * This extra is not strictly required, however it makes backward and forward compatibility significantly
     * easier. For example, suppose a bug is found in how some version of the plug-in stored its Bundle. By
     * having the version, the plug-in can better detect when such bugs occur.
     */
    public static final String BUNDLE_EXTRA_INT_VERSION_CODE =
    		BUNDLE_CLASS_NAME+".INT_VERSION_CODE"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_BOOLEAN_STATE =
    		BUNDLE_CLASS_NAME+".BOOLEAN_STATE"; //$NON-NLS-1$

    /**
     * Method to verify the content of the bundle are correct.
     * <p>
     * This method will not mutate {@code bundle}.
     *
     * @param bundle bundle to verify. May be null, which will always return false.
     * @return true if the Bundle is valid, false if the bundle is invalid.
     */
    public static boolean isBundleValid(final Bundle bundle)
    {
        if (null == bundle)
        {
            return false;
        }

        /*
         * Make sure the expected extras exist
         */
        if (!bundle.containsKey(BUNDLE_EXTRA_ACCOUNT_ID))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format("bundle must contain extra %s", BUNDLE_EXTRA_ACCOUNT_ID)); //$NON-NLS-1$
            }
            return false;
        }
        if (!bundle.containsKey(BUNDLE_EXTRA_INT_VERSION_CODE))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format("bundle must contain extra %s", BUNDLE_EXTRA_INT_VERSION_CODE)); //$NON-NLS-1$
            }
            return false;
        }

        /*
         * Make sure the correct number of extras exist. Run this test after checking for specific Bundle
         * extras above so that the error message is more useful. (E.g. the caller will see what extras are
         * missing, rather than just a message that there is the wrong number).
         */
        if (bundle.getLong(BUNDLE_EXTRA_ACCOUNT_ID,-1) == -1)
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format("bundle extra %s appears to be empty.  It must be 0+ Long", BUNDLE_EXTRA_ACCOUNT_ID)); //$NON-NLS-1$
            }
            return false;
        }

        if (bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 0) != bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 1))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format("bundle extra %s appears to be the wrong type.  It must be an int", BUNDLE_EXTRA_INT_VERSION_CODE)); //$NON-NLS-1$
            }

            return false;
        }

        return true;
    }

    /**
     * @param context Application context.
     * @param message The toast message to be displayed by the plug-in. Cannot be null.
     * @return A plug-in bundle.
     */
    public static Bundle generateBundle(final Context context, final long accountId)
    {
        final Bundle result = new Bundle();
        result.putInt(BUNDLE_EXTRA_INT_VERSION_CODE, Constants.getVersionCode(context));
        result.putLong(BUNDLE_EXTRA_ACCOUNT_ID, accountId);

        return result;
    }

    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginBundleManager()
    {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}