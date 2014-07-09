/*
 * Copyright (C) 2014 The OSE Project
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
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreferenceCham;
import com.android.settings.liquid.AppMultiSelectListPreference;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class PeekSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_PEEK = "notification_peek";
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_TIME = "notification_peek_time";

    private CheckBoxPreference mNotificationPeek;
    private ListPreference mPeekPickupTimeout;
    private SeekBarPreferenceCham mNotificationPeekTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.peek_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mNotificationPeek = (CheckBoxPreference) findPreference(KEY_PEEK);
        mNotificationPeek.setPersistent(false);

        updatePeekCheckbox();

        mPeekPickupTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_PICKUP_TIMEOUT);
        int peekTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, 0, UserHandle.USER_CURRENT);
        mPeekPickupTimeout.setValue(String.valueOf(peekTimeout));
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntry());
        mPeekPickupTimeout.setOnPreferenceChangeListener(this);

        mNotificationPeekTime = (SeekBarPreferenceCham) prefSet.findPreference(KEY_PEEK_TIME);
        mNotificationPeekTime.setValue(Settings.System.getInt(resolver,
                Settings.System.PEEK_TIME, 5000));
        mNotificationPeekTime.setOnPreferenceChangeListener(this);

    }

    private void updatePeekCheckbox() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.PEEK_STATE, 0) == 1;
        mNotificationPeek.setChecked(enabled);
    }

    private void updatePeekTimeoutOptions(Object newValue) {
        int index = mPeekPickupTimeout.findIndexOfValue((String) newValue);
        int value = Integer.valueOf((String) newValue);
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, value);
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntries()[index]);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNotificationPeek) {
            Settings.System.putInt(getContentResolver(), Settings.System.PEEK_STATE,
                    mNotificationPeek.isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mPeekPickupTimeout) {
            int peekTimeout = Integer.valueOf((String) value);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT,
                    peekTimeout, UserHandle.USER_CURRENT);
            updatePeekTimeoutOptions(value);
        } else if ( preference == mNotificationPeekTime) {
            int time = ((Integer)value).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.PEEK_TIME, time);
        }
        return true;
    }
}