package com.retroarch.browser.retroactivity;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Utility class to manage the secondary display presentation.
 */
public class DualScreenSupport {
    private static final String TAG = "DualScreenSupport";
    private static SecondaryDisplayPresentation mPresentation;
    private static String mLastJson;

    public static void updateSecondaryDisplay(final Context context, final String json) {
        mLastJson = json;
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();

        Display presentationDisplay = null;

        // Strategy: First look for any display that explicitly supports Presentation
        for (Display d : displays) {
            if (d.getDisplayId() == Display.DEFAULT_DISPLAY) continue;
            if ((d.getFlags() & Display.FLAG_PRESENTATION) != 0) {
                presentationDisplay = d;
                break;
            }
        }

        // Fallback: Pick the first non-default display
        if (presentationDisplay == null) {
            for (Display d : displays) {
                if (d.getDisplayId() != Display.DEFAULT_DISPLAY) {
                    presentationDisplay = d;
                    break;
                }
            }
        }

        if (presentationDisplay != null) {
            final Display display = presentationDisplay;
            Activity activity = getActivity(context);

            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showPresentation(activity, display, json);
                    }
                });
            } else {
                Log.e(TAG, "Context is not an Activity, cannot run on UI thread.");
            }
        }
    }

    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static void showPresentation(Activity activity, Display display, String json) {
        if (mPresentation != null && mPresentation.getDisplay().getDisplayId() != display.getDisplayId()) {
            Log.i(TAG, "Display changed, dismissing old presentation.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        if (mPresentation == null) {
            Log.i(TAG, "Creating new SecondaryDisplayPresentation on display: " + display.getName() + " (ID: " + display.getDisplayId() + ")");
            mPresentation = new SecondaryDisplayPresentation(activity, display);
            try {
                mPresentation.show();
                Log.i(TAG, "Presentation.show() called successfully.");
            } catch (Exception ex) {
                Log.e(TAG, "Failed to show presentation: " + ex.getMessage());
                mPresentation = null;
                return;
            }
        }

        mPresentation.updateData(json);
    }

    public static void clear() {
        if (mPresentation != null) {
            Log.i(TAG, "Clearing presentation.");
            mPresentation.dismiss();
            mPresentation = null;
        }
        mLastJson = null;
    }
}
