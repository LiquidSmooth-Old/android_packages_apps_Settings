/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.IWindowManager;

import java.util.ArrayList;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class BackToKill extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String KILL_APP_LONGPRESS_BACK_DELAY = "kill_app_longpress_back_delay";

    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
    private final ArrayList<CheckBoxPreference> mResetCbPrefs
            = new ArrayList<CheckBoxPreference>();

    private CheckBoxPreference mKillAppLongpressBack;
    ListPreference mKillAppLongpressBackDelay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.backtokill);

        mKillAppLongpressBack = findAndInitCheckboxPref(KILL_APP_LONGPRESS_BACK);
        mKillAppLongpressBackDelay = (ListPreference) findPreference(KILL_APP_LONGPRESS_BACK_DELAY);
        mKillAppLongpressBackDelay.setOnPreferenceChangeListener(this);
        mKillAppLongpressBackDelay.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK_DELAY, 1000) + "");
    }

    private CheckBoxPreference findAndInitCheckboxPref(String key) {
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        mAllPrefs.add(pref);
        mResetCbPrefs.add(pref);
        return pref;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateKillAppLongpressBackOptions();
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK,
                mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(
            getActivity().getContentResolver(), Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mKillAppLongpressBackDelay) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.KILL_APP_LONGPRESS_BACK_DELAY, val);
        } else if (preference == mKillAppLongpressBackDelay) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.KILL_APP_LONGPRESS_BACK_DELAY, val);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKillAppLongpressBack) {
            writeKillAppLongpressBackOptions();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}