/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.SettingsStore;
import org.mozilla.vrbrowser.WidgetPlacement;
import org.mozilla.vrbrowser.audio.AudioEngine;

public class DeveloperOptionsWidget extends UIWidget {

    private static final String LOGTAG = "VRB";

    public enum InputMode {
        MOUSE,
        TOUCH
    }

    private AudioEngine mAudio;
    private Switch mRemoteDebuggingSwitch;
    private Switch mConsoleLogsSwitch;
    private Switch mEnvOverrideSwitch;
    private Switch mDesktopVersionSwitch;
    private UIButton mBackButton;
    private RadioGroup mEventsRadio;
    private TextView mDensityButton;
    private TextView mWindowSizeButton;
    private TextView mDpiButton;
    private TextView mMaxWindowSizeButton;
    private int mRestartDialogHandle = -1;

    public DeveloperOptionsWidget(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public DeveloperOptionsWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public DeveloperOptionsWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.developer_options, this);

        mAudio = AudioEngine.fromContext(aContext);

        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                onBackButton();
            }
        });

        mRemoteDebuggingSwitch = findViewById(R.id.developer_options_remote_debugging_switch);
        mRemoteDebuggingSwitch.setChecked(SettingsStore.getInstance(getContext()).isRemoteDebuggingEnabled());
        mRemoteDebuggingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                SettingsStore.getInstance(getContext()).setRemoteDebuggingEnabled(b);

                showRestartDialog();
            }
        });

        mConsoleLogsSwitch = findViewById(R.id.developer_options_show_console_switch);
        mConsoleLogsSwitch.setChecked(SettingsStore.getInstance(getContext()).isConsoleLogsEnabled());
        mConsoleLogsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                SettingsStore.getInstance(getContext()).setConsoleLogsEnabled(b);

                showRestartDialog();
            }
        });

        mEnvOverrideSwitch = findViewById(R.id.developer_options_env_override_switch);
        mEnvOverrideSwitch.setChecked(SettingsStore.getInstance(getContext()).isEnvironmentOverrideEnabled());
        mEnvOverrideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                SettingsStore.getInstance(getContext()).setEnvironmentOverrideEnabled(b);

                showRestartDialog();
            }
        });

        mDesktopVersionSwitch = findViewById(R.id.developer_options_desktop_version_switch);
        mDesktopVersionSwitch.setChecked(SettingsStore.getInstance(getContext()).isDesktopVersionEnabled());
        mDesktopVersionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                SettingsStore.getInstance(getContext()).setDesktopVersionEnabled(b);

                showRestartDialog();
            }
        });

        mEventsRadio = findViewById(R.id.radioEvents);
        RadioButton mTouchRadio = findViewById(R.id.radioTouch);
        RadioButton mMouseRadio = findViewById(R.id.radioMouse);
        InputMode inputMode = InputMode.values()[SettingsStore.getInstance(getContext()).getInputMode()];
        if (inputMode == InputMode.MOUSE) {
            mTouchRadio.setChecked(false);
            mMouseRadio.setChecked(true);

        } else if (inputMode == InputMode.TOUCH) {
            mTouchRadio.setChecked(true);
            mMouseRadio.setChecked(false);
        }
        mEventsRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                if (checkedId == R.id.radioMouse) {
                    SettingsStore.getInstance(getContext()).setInputMode(InputMode.MOUSE.ordinal());

                } else if (checkedId == R.id.radioTouch) {
                    SettingsStore.getInstance(getContext()).setInputMode(InputMode.TOUCH.ordinal());
                }
            }
        });

        final TextView densityText = findViewById(R.id.densityText);
        densityText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getDisplayDensity()));
        final DeveloperOptionsEditText densityEdit = findViewById(R.id.densityEdit);
        densityEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getDisplayDensity()));
        densityEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mDensityButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        mDensityButton = findViewById(R.id.densityEditButton);
        mDensityButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                if (densityEdit.getVisibility() == View.VISIBLE) {
                    densityText.setText(densityEdit.getText());
                    densityText.setVisibility(View.VISIBLE);
                    densityEdit.setVisibility(View.GONE);

                    showRestartDialog();

                } else {
                    densityText.setVisibility(View.GONE);
                    densityEdit.setVisibility(View.VISIBLE);
                }

                SettingsStore.getInstance(getContext()).setDisplayDensity(Integer.parseInt(densityText.getText().toString()));
            }
        });

        final TextView windowWidthText = findViewById(R.id.windowSizeWidthText);
        windowWidthText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getWindowWidth()));
        final DeveloperOptionsEditText windowWidthEdit = findViewById(R.id.windowSizeWidthEdit);
        windowWidthEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getWindowWidth()));
        windowWidthEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mWindowSizeButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        final TextView windowHeightText = findViewById(R.id.windowSizeHeightText);
        windowHeightText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getWindowHeight()));
        final DeveloperOptionsEditText windowHeightEdit = findViewById(R.id.windowSizeHeightEdit);
        windowHeightEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getWindowHeight()));
        windowHeightEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mWindowSizeButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        mWindowSizeButton = findViewById(R.id.windowSizeEditButton);
        mWindowSizeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                if (windowWidthEdit.getVisibility() == View.VISIBLE) {
                    windowWidthText.setText(windowWidthEdit.getText());
                    windowHeightText.setText(windowHeightEdit.getText());
                    windowWidthText.setVisibility(View.VISIBLE);
                    windowHeightText.setVisibility(View.VISIBLE);
                    windowWidthEdit.setVisibility(View.GONE);
                    windowHeightEdit.setVisibility(View.GONE);

                } else {
                    windowWidthText.setVisibility(View.GONE);
                    windowHeightText.setVisibility(View.GONE);
                    windowWidthEdit.setVisibility(View.VISIBLE);
                    windowHeightEdit.setVisibility(View.VISIBLE);
                }

                int windowWidth = Integer.parseInt(windowWidthText.getText().toString());
                int windowHeight = Integer.parseInt(windowHeightText.getText().toString());

                SettingsStore.getInstance(getContext()).setWindowWidth(windowWidth);
                SettingsStore.getInstance(getContext()).setWindowHeight(windowHeight);

                mWidgetManager.setBrowserSize(windowWidth, windowHeight);
            }
        });

        //
        final TextView dpiText = findViewById(R.id.dpiText);
        dpiText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getDisplayDpi()));
        final DeveloperOptionsEditText dpiEdit = findViewById(R.id.dpiEdit);
        dpiEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getDisplayDpi()));
        dpiEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mDpiButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        mDpiButton = findViewById(R.id.dpiEditButton);
        mDpiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                if (dpiEdit.getVisibility() == View.VISIBLE) {
                    dpiText.setText(dpiEdit.getText());
                    dpiText.setVisibility(View.VISIBLE);
                    dpiEdit.setVisibility(View.GONE);

                    showRestartDialog();

                } else {
                    dpiText.setVisibility(View.GONE);
                    dpiEdit.setVisibility(View.VISIBLE);
                }

                SettingsStore.getInstance(getContext()).setDisplayDpi(Integer.parseInt(dpiText.getText().toString()));
            }
        });

        final TextView maxWindowWidthText = findViewById(R.id.maxWindowSizeWidthText);
        maxWindowWidthText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getMaxWindowWidth()));
        final DeveloperOptionsEditText maxWindowWidthEdit = findViewById(R.id.maxWindowSizeWidthEdit);
        maxWindowWidthEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getMaxWindowWidth()));
        maxWindowWidthEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mMaxWindowSizeButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        final TextView maxWindowHeightText = findViewById(R.id.maxWindowSizeHeightText);
        maxWindowHeightText.setText(Integer.toString(SettingsStore.getInstance(getContext()).getMaxWindowHeight()));
        final DeveloperOptionsEditText maxWindowHeightEdit = findViewById(R.id.maxWindowSizeHeightEdit);
        maxWindowHeightEdit.setText(Integer.toString(SettingsStore.getInstance(getContext()).getMaxWindowHeight()));
        maxWindowHeightEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mMaxWindowSizeButton.callOnClick();
                    return true;
                }

                return false;
            }
        });
        mMaxWindowSizeButton = findViewById(R.id.maxWindowSizeEditButton);
        mMaxWindowSizeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudio != null) {
                    mAudio.playSound(AudioEngine.Sound.CLICK);
                }

                if (maxWindowWidthEdit.getVisibility() == View.VISIBLE) {
                    maxWindowWidthText.setText(maxWindowWidthEdit.getText());
                    maxWindowHeightText.setText(maxWindowHeightEdit.getText());
                    maxWindowWidthText.setVisibility(View.VISIBLE);
                    maxWindowHeightText.setVisibility(View.VISIBLE);
                    maxWindowWidthEdit.setVisibility(View.GONE);
                    maxWindowHeightEdit.setVisibility(View.GONE);

                } else {
                    maxWindowWidthText.setVisibility(View.GONE);
                    maxWindowHeightText.setVisibility(View.GONE);
                    maxWindowWidthEdit.setVisibility(View.VISIBLE);
                    maxWindowHeightEdit.setVisibility(View.VISIBLE);
                }

                SettingsStore.getInstance(getContext()).setMaxWindowWidth(Integer.parseInt(maxWindowWidthText.getText().toString()));
                SettingsStore.getInstance(getContext()).setMaxWindowHeight(Integer.parseInt(maxWindowHeightText.getText().toString()));
            }
        });
    }

    @Override
    void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.visible = false;
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.developer_options_width);
        aPlacement.height = WidgetPlacement.dpDimension(getContext(), R.dimen.developer_options_height);
        aPlacement.parentAnchorX = 0.5f;
        aPlacement.parentAnchorY = 0.5f;
        aPlacement.anchorX = 0.5f;
        aPlacement.anchorY = 0.5f;
        aPlacement.translationY = WidgetPlacement.unitFromMeters(getContext(), R.dimen.restart_dialog_world_y);
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.restart_dialog_world_z);
    }

    private void showRestartDialog() {
        UIWidget widget = getChild(mRestartDialogHandle);
        if (widget == null) {
            widget = createChild(RestartDialogWidget.class, false);
            mRestartDialogHandle = widget.getHandle();
        }

        widget.show();

        hide();
    }

}
