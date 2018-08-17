package org.mozilla.vrbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.mozilla.telemetry.TelemetryHolder;
import org.mozilla.vrbrowser.telemetry.TelemetryWrapper;
import org.mozilla.vrbrowser.ui.DeveloperOptionsWidget;


public class SettingsStore {

    private static final String LOGTAG = "VRB";

    private static SettingsStore mSettingsInstance;

    public static synchronized @NonNull
    SettingsStore getInstance(final @NonNull Context aContext) {
        if (mSettingsInstance == null) {
            mSettingsInstance = new SettingsStore(aContext);
        }

        return mSettingsInstance;
    }

    private Context mContext;
    private SharedPreferences mPrefs;

    // Developer options default values
    private final static boolean REMOTE_DEBUGGING_DEFAULT = false;
    private final static boolean CONSOLE_LOGS_DEFAULT = false;
    private final static boolean ENV_OVERRIDE_DEFAULT = false;
    private final static boolean DESKTOP_VERSION_DEFAULT = false;
    private final static DeveloperOptionsWidget.InputMode INPUT_MODE_DEFAULT = DeveloperOptionsWidget.InputMode.TOUCH;
    private final static int DISPLAY_DENSITY_DEFAULT = 2;
    private final static int WINDOW_WIDTH_DEFAULT = 1024;
    private final static int WINDOW_HEIGHT_DEFAULT = 768;
    private final static int DISPLAY_DPI_DEFAULT = 96;
    private final static int MAX_WINDOW_WIDTH_DEFAULT = 1024;
    private final static int MAX_WINDOW_HEIGHT_DEFAULT = 768;

    // Enable telemetry by default (opt-out).
    private final static boolean enableCrashReportingByDefault = false;
    private final static boolean enableTelemetryByDefault = true;

    public SettingsStore(Context aContext) {
        mContext = aContext;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(aContext);
    }

    public boolean isCrashReportingEnabled() {
        return mPrefs.getBoolean(mContext.getString(R.string.settings_key_crash), enableCrashReportingByDefault);
    }

    public void setCrashReportingEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_crash), isEnabled);
        editor.commit();
    }

    public boolean isTelemetryEnabled() {
        // The first access to shared preferences will require a disk read.
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            return mPrefs.getBoolean(
                    mContext.getString(R.string.settings_key_telemetry), enableTelemetryByDefault);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    public void setTelemetryEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_telemetry), isEnabled);
        editor.commit();

        // If the state of Telemetry is not the same, we reinitialize it.
        final boolean hasEnabled = isTelemetryEnabled();
        if (hasEnabled != isEnabled) {
            TelemetryWrapper.init(mContext);
        }

        TelemetryHolder.get().getConfiguration().setUploadEnabled(isEnabled);
        TelemetryHolder.get().getConfiguration().setCollectionEnabled(isEnabled);
    }

    public boolean isRemoteDebuggingEnabled() {
        return mPrefs.getBoolean(
                mContext.getString(R.string.settings_key_console_logs), REMOTE_DEBUGGING_DEFAULT);
    }

    public void setRemoteDebuggingEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_remote_debugging), isEnabled);
        editor.commit();
    }

    public boolean isConsoleLogsEnabled() {
        return mPrefs.getBoolean(
                mContext.getString(R.string.settings_key_console_logs), CONSOLE_LOGS_DEFAULT);
    }

    public void setConsoleLogsEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_console_logs), isEnabled);
        editor.commit();
    }

    public boolean isEnvironmentOverrideEnabled() {
        return mPrefs.getBoolean(
                mContext.getString(R.string.settings_key_environment_override), ENV_OVERRIDE_DEFAULT);
    }

    public void setEnvironmentOverrideEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_environment_override), isEnabled);
        editor.commit();
    }

    public boolean isDesktopVersionEnabled() {
        return mPrefs.getBoolean(
                mContext.getString(R.string.settings_key_desktop_version), DESKTOP_VERSION_DEFAULT);
    }

    public void setDesktopVersionEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(mContext.getString(R.string.settings_key_desktop_version), isEnabled);
        editor.commit();
    }

    public int getInputMode() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_input_mode), INPUT_MODE_DEFAULT.ordinal());
    }

    public void setInputMode(int aTouchMode) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_input_mode), aTouchMode);
        editor.commit();
    }


    public int getDisplayDensity() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_display_density), DISPLAY_DENSITY_DEFAULT);
    }

    public void setDisplayDensity(int aDensity) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_display_density), aDensity);
        editor.commit();
    }

    public int getWindowWidth() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_window_width), WINDOW_WIDTH_DEFAULT);
    }

    public void setWindowWidth(int aWindowWidth) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_window_width), aWindowWidth);
        editor.commit();
    }

    public int getWindowHeight() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_window_height), WINDOW_HEIGHT_DEFAULT);
    }

    public void setWindowHeight(int aWindowHeight) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_window_height), aWindowHeight);
        editor.commit();
    }

    //
    public int getDisplayDpi() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_display_dpi), DISPLAY_DPI_DEFAULT);
    }

    public void setDisplayDpi(int aDpi) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_display_dpi), aDpi);
        editor.commit();
    }

    public int getMaxWindowWidth() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_max_window_width), MAX_WINDOW_WIDTH_DEFAULT);
    }

    public void setMaxWindowWidth(int aMaxWindowWidth) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_max_window_width), aMaxWindowWidth);
        editor.commit();
    }

    public int getMaxWindowHeight() {
        return mPrefs.getInt(
                mContext.getString(R.string.settings_key_max_window_height), MAX_WINDOW_HEIGHT_DEFAULT);
    }

    public void setMaxWindowHeight(int aMaxWindowHeight) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(mContext.getString(R.string.settings_key_max_window_height), aMaxWindowHeight);
        editor.commit();
    }
}
