/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.liquid.SeekBarPreference;
import com.android.internal.util.liquid.DeviceUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSettings extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_NOTIFICATION_COUNT = "status_bar_notification_count";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String KEY_EXPANDED_DESKTOP = "expanded_desktop";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";
    private static final String STATUSBAR_6BAR_SIGNAL = "statusbar_6bar_signal";
    private static final String TOGGLE_CARRIER_LOGO = "toggle_carrier_logo";
    private static final String PREF_TICKER = "ticker_disabled";

    static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

    private PreferenceScreen mClockStyle;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mStatusBarNotifCount;
    private ListPreference mStatusBarSignal;
    private ListPreference mExpandedDesktopPref;
    private CheckBoxPreference mStatusBarCarrier;
    private CheckBoxPreference mToggleCarrierLogo;
    private ColorPickerPreference mCarrierColorPicker;
    private CheckBoxPreference mStatusBarSixBarSignal;
    private CheckBoxPreference mTicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.liquid_statusbar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        // Expanded desktop
        mExpandedDesktopPref = (ListPreference) findPreference(KEY_EXPANDED_DESKTOP);
        int expandedDesktopValue = Settings.System.getInt(getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_STYLE, 2);

        PreferenceCategory mCategory = (PreferenceCategory) findPreference("advanced_category");

        try {
            boolean navBarEnabled = (Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_SHOW, 0) == 1) ||
                WindowManagerGlobal.getWindowManagerService().hasNavigationBar();

            if (navBarEnabled) {
                mExpandedDesktopPref.setOnPreferenceChangeListener(this);
                mExpandedDesktopPref.setValue(String.valueOf(expandedDesktopValue));
                updateExpandedDesktop(expandedDesktopValue);
            } else {
                mCategory.removePreference(mExpandedDesktopPref);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }

        // Start observing for changes on auto brightness
        StatusBarBrightnessChangedObserver statusBarBrightnessChangedObserver =
            new StatusBarBrightnessChangedObserver(new Handler());
        statusBarBrightnessChangedObserver.startObserving();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

        mStatusBarBrightnessControl =
            (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIFICATION_COUNT);
        mStatusBarNotifCount.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_NOTIFICATION_COUNT, 0) == 1));

        mStatusBarSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
        int signalStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarSignal.setValue(String.valueOf(signalStyle));
        mStatusBarSignal.setSummary(mStatusBarSignal.getEntry());
        mStatusBarSignal.setOnPreferenceChangeListener(this);

        mTicker = (CheckBoxPreference) prefSet.findPreference(PREF_TICKER);
        mTicker.setChecked(Settings.System.getInt(resolver,
                Settings.System.TICKER_DISABLED, 0) == 1);
        mTicker.setOnPreferenceChangeListener(this);

        mStatusBarCarrier = (CheckBoxPreference) findPreference(STATUS_BAR_CARRIER);
        mStatusBarCarrier.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CARRIER, 0) == 1));

        mToggleCarrierLogo = (CheckBoxPreference) findPreference(TOGGLE_CARRIER_LOGO);
        mToggleCarrierLogo.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.TOGGLE_CARRIER_LOGO, 0) == 1));

        mCarrierColorPicker = (ColorPickerPreference) findPreference(STATUS_BAR_CARRIER_COLOR);
        mCarrierColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_CARRIER_COLOR);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mCarrierColorPicker.setSummary(hexColor);
        mCarrierColorPicker.setNewPreviewColor(intColor);

        // 6 bar signal
        mStatusBarSixBarSignal = (CheckBoxPreference) findPreference(STATUSBAR_6BAR_SIGNAL);
        mStatusBarSixBarSignal.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_6BAR_SIGNAL, 0) == 1));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mExpandedDesktopPref) {
            int expandedDesktopValue = Integer.valueOf((String) newValue);
            updateExpandedDesktop(expandedDesktopValue);
            return true;
        } else if (preference == mStatusBarSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarSignal.setSummary(mStatusBarSignal.getEntries()[index]);
            return true;
        } else if (preference == mCarrierColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
            return true;
        } else if (preference == mTicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.TICKER_DISABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIFICATION_COUNT,
                    mStatusBarNotifCount.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mStatusBarCarrier) {
           Settings.System.putInt(getContentResolver(),
                   Settings.System.STATUS_BAR_CARRIER,
                   mStatusBarCarrier.isChecked() ? 1 : 0);
           return true;
        } else if (preference == mStatusBarSixBarSignal) {
            Settings.System.putInt(getContentResolver(),
                   Settings.System.STATUSBAR_6BAR_SIGNAL,
                   mStatusBarSixBarSignal.isChecked() ? 1 : 0);
           return true;
        } else if (preference == mToggleCarrierLogo) {
            Settings.System.putInt(getContentResolver(),
                   Settings.System.TOGGLE_CARRIER_LOGO,
                   mToggleCarrierLogo.isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
        updateStatusBarBrightnessControl();
    }

    private void updateStatusBarBrightnessControl() {
        try {
            if (mStatusBarBrightnessControl != null) {
                int mode = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mStatusBarBrightnessControl.setEnabled(false);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                } else {
                    mStatusBarBrightnessControl.setEnabled(true);
                    mStatusBarBrightnessControl.setSummary(
                        R.string.status_bar_toggle_brightness_summary);
                }
            }
        } catch (SettingNotFoundException e) {
        }
    }

    private void updateExpandedDesktop(int value) {
        ContentResolver cr = getContentResolver();
        Resources res = getResources();
        int summary = -2;

        Settings.System.putInt(cr, Settings.System.EXPANDED_DESKTOP_STYLE, value);

        if (value == 1) {
            summary = R.string.expanded_desktop_status_bar;
        } else if (value == 2) {
            summary = R.string.expanded_desktop_no_status_bar;
        }

        if (mExpandedDesktopPref != null && summary != -2) {
            mExpandedDesktopPref.setSummary(res.getString(summary));
        }
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
        }
    }

    private class StatusBarBrightnessChangedObserver extends ContentObserver {
        public StatusBarBrightnessChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStatusBarBrightnessControl();
        }

        public void startObserving() {
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, this);
        }
    }
}
