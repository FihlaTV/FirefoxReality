/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.vrbrowser.*;
import org.mozilla.vrbrowser.audio.AudioEngine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SettingsWidget extends UIWidget {
    private static final String LOGTAG = "VRB";

    private static final int RESTART_DIALOG_ID = 0;
    private static final int DEVELOPER_OPTIONS_DIALOG_ID = 1;

    private AudioEngine mAudio;
    private RestartDialogWidget mRestartWidget;
    private DeveloperOptionsWidget mDeveloperOptionsWidget;
    private Runnable mBackHandler;
    private TextView mBuildText;

    class VersionGestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean mIsHash;

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (mIsHash)
                mBuildText.setText(versionCodeToDate(BuildConfig.VERSION_CODE));
            else
                mBuildText.setText(BuildConfig.GIT_HASH);

            mIsHash = !mIsHash;

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    public SettingsWidget(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public SettingsWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public SettingsWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.settings, this);

        ImageButton cancelButton = findViewById(R.id.settingsCancelButton);

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                hide();
            }
        });

        SettingsButton privacyButton = findViewById(R.id.privacyButton);
        privacyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onSettingsPrivacyClick();
            }
        });

        Switch crashReportingSwitch  = findViewById(R.id.crash_reporting_switch);
        crashReportingSwitch.setChecked(SettingsStore.getInstance(getContext()).isCrashReportingEnabled());
        crashReportingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onSettingsCrashReportingChange(b);
            }
        });

        Switch telemetrySwitch  = findViewById(R.id.telemetry_switch);
        telemetrySwitch.setChecked(SettingsStore.getInstance(getContext()).isTelemetryEnabled());
        telemetrySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onSettingsTelemetryChange(b);
            }
        });

        TextView versionText = findViewById(R.id.versionText);
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionText.setText(String.format(getResources().getString(R.string.settings_version), pInfo.versionName));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mBuildText = findViewById(R.id.buildText);
        mBuildText.setText(versionCodeToDate(BuildConfig.VERSION_CODE));

        ViewGroup versionLayout = findViewById(R.id.versionLayout);
        final GestureDetector gd = new GestureDetector(getContext(), new VersionGestureListener());
        versionLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gd.onTouchEvent(motionEvent)) {
                    return true;
                }
                return view.onTouchEvent(motionEvent);
            }
        });

        SettingsButton reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onSettingsReportClick();
            }
        });

        SettingsButton developerOptionsButton = findViewById(R.id.developerOptionsButton);
        developerOptionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onDeveloperOptionsClick();
            }
        });

        mAudio = AudioEngine.fromContext(aContext);

        mBackHandler = new Runnable() {
            @Override
            public void run() {
                hide();
            }
        };
    }

    @Override
    void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.visible = false;
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.settings_width);
        aPlacement.height = WidgetPlacement.dpDimension(getContext(), R.dimen.settings_height);
        aPlacement.parentAnchorX = 0.5f;
        aPlacement.parentAnchorY = 0.5f;
        aPlacement.anchorX = 0.5f;
        aPlacement.anchorY = 0.5f;
        aPlacement.translationY = WidgetPlacement.unitFromMeters(getContext(), R.dimen.settings_world_y);
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.settings_world_z);
    }

    private void onSettingsCrashReportingChange(boolean isEnabled) {
        SettingsStore.getInstance(getContext()).setCrashReportingEnabled(isEnabled);

        mRestartWidget = createChild(RESTART_DIALOG_ID, RestartDialogWidget.class);
        mRestartWidget.show();

        hide();
    }

    private void onSettingsTelemetryChange(boolean isEnabled) {
        SettingsStore.getInstance(getContext()).setTelemetryEnabled(isEnabled);
        // TODO: Waiting for Telemetry to be merged
    }

    private void onSettingsPrivacyClick() {
        SessionStore.SessionSettings settings = new SessionStore.SessionSettings();
        GeckoSession session = SessionStore.get().getCurrentSession();
        if (session == null) {
            int sessionId = SessionStore.get().createSession(settings);
            SessionStore.get().setCurrentSession(sessionId);
        }

        SessionStore.get().loadUri(getContext().getString(R.string.private_policy_url));

        hide();
    }

    private void onSettingsReportClick() {
        String url = SessionStore.get().getCurrentUri();

        SessionStore.SessionSettings settings = new SessionStore.SessionSettings();
        GeckoSession session = SessionStore.get().getCurrentSession();
        if (session == null) {
            int sessionId = SessionStore.get().createSession(settings);
            SessionStore.get().setCurrentSession(sessionId);
        }

        try {
            // In case the user had no active sessions when reporting just leave the URL field empty
            url = (url != null) ? URLEncoder.encode(url, "UTF-8") : "";

        } catch (UnsupportedEncodingException e) {
            Log.e(LOGTAG, "Cannot encode URL");
        }
        SessionStore.get().loadUri(getContext().getString(R.string.private_report_url, url));

        hide();
    }

    private void onDeveloperOptionsClick() {
        mDeveloperOptionsWidget = createChild(DEVELOPER_OPTIONS_DIALOG_ID, DeveloperOptionsWidget.class);
        mDeveloperOptionsWidget.show();

        hide();
    }

    protected void onChildShown(int aChildId) {
        hide();
    }

    protected void onChildHidden(int aChildId) {
        show();
    }

    /**
     * The version code is composed like: yDDDHHmm
     *  * y   = Double digit year, with 16 substracted: 2017 -> 17 -> 1
     *  * DDD = Day of the year, pad with zeros if needed: September 6th -> 249
     *  * HH  = Hour in day (00-23)
     *  * mm  = Minute in hour
     *
     * For September 6th, 2017, 9:41 am this will generate the versionCode: 12490941 (1-249-09-41).
     *
     * For local debug builds we use a fixed versionCode to not mess with the caching mechanism of the build
     * system. The fixed local build number is 1.
     *
     * @param aVersionCode
     * @return String The converted date in the format yyyy-MM-dd
     */
    private String versionCodeToDate(int aVersionCode) {
        String versionCode = Integer.toString(aVersionCode);

        String formatted;
        try {
            int year = Integer.parseInt(versionCode.substring(1, 2)) + 2016;
            int dayOfYear = Integer.parseInt(versionCode.substring(2, 5));

            GregorianCalendar cal = (GregorianCalendar)GregorianCalendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_YEAR, dayOfYear);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            formatted = format.format(cal.getTime());

        } catch (StringIndexOutOfBoundsException e) {
            formatted = getContext().getString(R.string.settings_version_developer);
        }

        return formatted;
    }
}
