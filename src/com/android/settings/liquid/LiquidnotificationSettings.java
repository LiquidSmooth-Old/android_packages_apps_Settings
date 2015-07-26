/*
 * Copyright (C) 2015 The LiquidSmooth Project
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

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LiquidnotificationSettings extends SettingsPreferenceFragment {

    private static final String PREF_HEADS_UP_GLOBAL_SWITCH = "heads_up_global_switch";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";

    private ListPreference mHeadsUpGlobalSwitch;
    private ListPreference mHeadsUpSnoozeTime;
    private ListPreference mHeadsUpTimeOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.liquid_notifications_settings);

        Resources systemUiResources;
        try {
            systemUiResources =
                    getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int headsUpSnoozeTime = Integer.valueOf((String) newValue);
                updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.HEADS_UP_SNOOZE_TIME,
                        headsUpSnoozeTime);
            }
        });
        final int defaultSnoozeTime = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_snooze_time", null, null));
        final int headsUpSnoozeTime = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_SNOOZE_TIME, defaultSnoozeTime);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnoozeTime));
        updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);

        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int headsUpTimeOut = Integer.valueOf((String) newValue);
                updateHeadsUpTimeOutSummary(headsUpTimeOut);
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.HEADS_UP_NOTIFCATION_DECAY,
                        headsUpTimeOut);
            }
        });
        final int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        final int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFCATION_DECAY, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

        mHeadsUpGlobalSwitch = (ListPreference) findPreference(PREF_HEADS_UP_GLOBAL_SWITCH);
        mHeadsUpGlobalSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int headsUpGlobalSwitch = Integer.valueOf((String) newValue);
                updateHeadsUpGlobalSwitchSummary(headsUpGlobalSwitch);
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.HEADS_UP_GLOBAL_SWITCH,
                        headsUpGlobalSwitch);
            }
        });
        final int headsUpGlobalSwitch = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_GLOBAL_SWITCH, 1);
        mHeadsUpGlobalSwitch.setValue(String.valueOf(headsUpGlobalSwitch));
        updateHeadsUpGlobalSwitchSummary(headsUpGlobalSwitch);

    }

    private void updateHeadsUpGlobalSwitchSummary(int value) {
        String summary;
        switch (value) {
            case 0:     summary = getResources().getString(
                                    R.string.heads_up_global_switch_summary_disabled);
                        mHeadsUpSnoozeTime.setEnabled(false);
                        mHeadsUpTimeOut.setEnabled(false);
                        break;
            case 1:     summary = getResources().getString(
                                    R.string.heads_up_global_switch_summary_perapp);
                        mHeadsUpSnoozeTime.setEnabled(true);
                        mHeadsUpTimeOut.setEnabled(true);
                        break;
            case 2:     summary = getResources().getString(
                                    R.string.heads_up_global_switch_summary_forced);
                        mHeadsUpSnoozeTime.setEnabled(true);
                        mHeadsUpTimeOut.setEnabled(true);
                        break;
            default:    summary = "";
                        break;
        }
        mHeadsUpGlobalSwitch.setSummary(summary);
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        String summary = value != 0
                ? getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000)
                : getResources().getString(R.string.heads_up_snooze_disabled_summary);
        mHeadsUpSnoozeTime.setSummary(summary);
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        if (value == 0) {
            mHeadsUpTimeOut.setSummary(
                    getResources().getString(R.string.heads_up_time_out_never_summary));
        } else {
            mHeadsUpTimeOut.setSummary(summary);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {

        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
 		return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}