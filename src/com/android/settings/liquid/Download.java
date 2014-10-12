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

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class Download extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    Preference mLSGapps;
    Preference mLSOfficial;
    Preference mLSNightly;
    Preference mPAGapps;
    Preference mXposed;
    Preference mXposedMod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.download);

        final ContentResolver resolver = getActivity().getContentResolver();

        mLSGapps = findPreference("ls_gapps");
        mLSOfficial = findPreference("ls_official");
        mLSNightly = findPreference("ls_nightly");
        mPAGapps = findPreference("pa_gapps");
        mXposed = findPreference("xposed");
        mXposedMod = findPreference("xposed_mod");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLSGapps) {
            Uri uri = Uri.parse("http://goo.gl/Skc3E0");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mPAGapps) {
            Uri uri = Uri.parse("http://goo.gl/9FbGFB");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mXposed) {
            Uri uri = Uri.parse("http://goo.gl/Mp0dTP");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mXposedMod) {
            Uri uri = Uri.parse("http://goo.gl/5J860t");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mLSOfficial) {
            Uri uri = Uri.parse("http://goo.gl/L1ho8B");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (preference == mLSNightly) {
            Uri uri = Uri.parse("http://goo.gl/xiEed1");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
         return true;
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}
