/*
 * Copyright (C) 2012 Slimroms
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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.util.liquid.DeviceUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class NotificationDrawerSettings extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener  {

    public static final String TAG = "NotificationDrawerSettings";

    private static final String PREF_NOTIFICATION_HIDE_LABELS =
            "notification_hide_labels";

    ListPreference mHideLabels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notification_drawer_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mHideLabels = (ListPreference) findPreference(PREF_NOTIFICATION_HIDE_LABELS);
        int hideCarrier = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_HIDE_LABELS, 0);
        mHideLabels.setValue(String.valueOf(hideCarrier));
        mHideLabels.setOnPreferenceChangeListener(this);
        updateHideNotificationLabelsSummary(hideCarrier);

        if (!DeviceUtils.isPhone(getActivity())) {
            // Nothing for tablets and large screen devices which doesn't show
            // information in notification drawer.....remove option
            prefs.removePreference(mHideLabels);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHideLabels) {
            int hideLabels = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_HIDE_LABELS,
                    hideLabels);
            updateHideNotificationLabelsSummary(hideLabels);
            return true;
        }
        return false;
    }

    private void updateHideNotificationLabelsSummary(int value) {
        Resources res = getResources();
        StringBuilder text = new StringBuilder();
        switch (value) {
            case 1:
                text.append(res.getString(R.string.notification_hide_labels_carrier));
                break;
            case 2:
                text.append(res.getString(R.string.notification_hide_labels_wifi));
                break;
            case 3:
                text.append(res.getString(R.string.notification_hide_labels_all));
                break;
            default:
                text.append(res.getString(R.string.notification_hide_labels_disable));
                break;
        }
        text.append(" " + res.getString(R.string.notification_hide_labels_text));
        mHideLabels.setSummary(text.toString());
    }

}
