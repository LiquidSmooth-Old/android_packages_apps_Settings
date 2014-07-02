/*
 * Copyright (C) 2014 TheKang
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Gravity;

import com.android.internal.util.liquid.DeviceUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.liquid.util.Helpers;

public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        UpdateSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateSettings();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void UpdateSettings() {
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //if (preference == mDummy) {
        //    int value = Integer.parseInt((String) newValue);
        //    Settings.System.putInt(getContentResolver(),
        //            Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
        //    return true;
        //}
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //if (preference == mHfmDisableAds) {
        //    boolean checked = ((CheckBoxPreference)preference).isChecked();
        //    Settings.System.putInt(getActivity().getContentResolver(),
        //            Settings.System.HFM_DISABLE_ADS, checked ? 1:0);
        //    HfmHelpers.checkStatus(getActivity());
        //    return true;
        //}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}