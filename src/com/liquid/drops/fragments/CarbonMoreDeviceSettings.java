/*
 * Copyright (C) 2013 The CyanogenMod project
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

package com.carbon.fibers.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;

public class CarbonMoreDeviceSettings extends SettingsPreferenceFragment {
    private static final String TAG = "CarbonMoreDeviceSettings";

    private static final String KEY_ADVANCED_DEVICE_SETTINGS = "advanced_device_settings";
    private static final String KEY_SPECIFIC_GESTURE_SETTINGS = "device_specific_gesture_settings";

    private PreferenceScreen mAdvancedDeviceSettings;
    private PreferenceScreen mSpecificGestureSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.carbon_more_device_settings);

        mAdvancedDeviceSettings = (PreferenceScreen) findPreference(KEY_ADVANCED_DEVICE_SETTINGS);
       if (!deviceSettingsAppExists(mAdvancedDeviceSettings)) {
            getPreferenceScreen().removePreference(mAdvancedDeviceSettings);
        }

        mSpecificGestureSettings = (PreferenceScreen) findPreference(KEY_SPECIFIC_GESTURE_SETTINGS);
        if (!deviceSettingsAppExists(mSpecificGestureSettings)) {
            getPreferenceScreen().removePreference(mSpecificGestureSettings);
        }
    }

    private boolean deviceSettingsAppExists(PreferenceScreen mynewValue) {
        Intent intent = mynewValue.getIntent();
        if (intent != null) {
            PackageManager pm = getActivity().getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
            int listSize = list.size();
            return (listSize > 0) ? true : false;

        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}