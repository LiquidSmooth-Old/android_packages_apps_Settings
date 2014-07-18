package com.android.settings.liquid;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DoubleTapToSleepSettings extends SettingsPreferenceFragment {

    private static final String DOUBLE_TAP_SLEEP_STATUS_BAR = "double_tap_sleep_status_bar";
    private static final String DOUBLE_TAP_SLEEP_GLOWPAD = "double_tap_sleep_glowpad";
    private static final String DOUBLE_TAP_SLEEP_STATUS_PIN_PASSWORD = "double_tap_sleep_pin_password";
    private static final String DOUBLE_TAP_SLEEP_PATTERN = "double_tap_sleep_pattern";

    private CheckBoxPreference mStatusBarDoubleTapSleepStatusBar;
    private CheckBoxPreference mStatusBarDoubleTapSleepGlowpad;
    private CheckBoxPreference mStatusBarDoubleTapSleepPinPassword;
    private CheckBoxPreference mStatusBarDoubleTapSleepPattern;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.double_tap_sleep_settings);

        // Status bar
        mStatusBarDoubleTapSleepStatusBar = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(DOUBLE_TAP_SLEEP_STATUS_BAR);
        mStatusBarDoubleTapSleepStatusBar.setChecked((Settings.System.getInt(getActivity()
                .getApplicationContext().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_STATUS_BAR, 0) == 1));

        // Glowpad
        mStatusBarDoubleTapSleepGlowpad = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(DOUBLE_TAP_SLEEP_GLOWPAD);
        mStatusBarDoubleTapSleepGlowpad.setChecked((Settings.System.getInt(getActivity()
                .getApplicationContext().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_GLOWPAD, 0) == 1));

        // Pin password
        mStatusBarDoubleTapSleepPinPassword = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(DOUBLE_TAP_SLEEP_STATUS_PIN_PASSWORD);
        mStatusBarDoubleTapSleepPinPassword.setChecked((Settings.System.getInt(getActivity()
                .getApplicationContext().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_STATUS_PIN_PASSWORD, 0) == 1));

        // Pattern
        mStatusBarDoubleTapSleepPattern = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(DOUBLE_TAP_SLEEP_PATTERN);
        mStatusBarDoubleTapSleepPattern.setChecked((Settings.System.getInt(getActivity()
                .getApplicationContext().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_PATTERN, 0) == 1));
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        // Status bar
        if (preference == mStatusBarDoubleTapSleepStatusBar) {
            value = mStatusBarDoubleTapSleepStatusBar.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_STATUS_BAR, value ? 1 : 0);
            return true;
        }

        // Glowpad
        else if (preference == mStatusBarDoubleTapSleepGlowpad) {
            value = mStatusBarDoubleTapSleepGlowpad.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GLOWPAD, value ? 1 : 0);
            return true;
        }

        // Pin password
        else if (preference == mStatusBarDoubleTapSleepPinPassword) {
            value = mStatusBarDoubleTapSleepPinPassword.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_STATUS_PIN_PASSWORD, value ? 1 : 0);
            return true;
        }

        // Pattern
        else if (preference == mStatusBarDoubleTapSleepPattern) {
            value = mStatusBarDoubleTapSleepPattern.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_PATTERN, value ? 1 : 0);
            return true;
        }

        return false;
    }
}
