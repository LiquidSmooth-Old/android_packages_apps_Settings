/*
 * Copyright (C) 2013 The LiquidSmooth Project
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
import android.app.admin.DevicePolicyManager;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.liquid.DeviceUtils;

public class LockscreenSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "LockscreenSettings";

    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String KEY_INTERFACE_SETTINGS = "lockscreen_interface";
    private static final String KEY_TARGET_SETTINGS = "lockscreen_targets";
    private static final String KEY_WIDGETS_SETTINGS = "lockscreen_widgets";
    private static final String KEY_GENERAL_CATEGORY = "general_category";
    private static final String KEY_BATTERY_AROUND_RING = "battery_around_ring";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String KEY_LOCK_BEFORE_UNLOCK = "lock_before_unlock";
    private static final String KEY_QUICK_UNLOCK_CONTROL = "quick_unlock_control";

    private PackageManager mPM;
    private DevicePolicyManager mDPM;
    private Preference mLockscreenWidgets;

    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mBatteryStatus;
    private ListPreference mLockscreenRotation;
    private CheckBoxPreference mLockBeforeUnlock;
    private CheckBoxPreference mLockQuickUnlock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.liquid_lockscreen_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mLockRingBattery = (CheckBoxPreference) prefs
                .findPreference(KEY_BATTERY_AROUND_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
        }

        mLockBeforeUnlock = (CheckBoxPreference) prefs
                .findPreference(KEY_LOCK_BEFORE_UNLOCK);
        if (mLockBeforeUnlock != null) {
            mLockBeforeUnlock.setChecked(Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_BEFORE_UNLOCK, 0) == 1);
            mLockBeforeUnlock.setOnPreferenceChangeListener(this);
        }

        mLockQuickUnlock = (CheckBoxPreference) prefs
                .findPreference(KEY_QUICK_UNLOCK_CONTROL);
        if (mLockQuickUnlock != null) {
            mLockQuickUnlock.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
        }

        mPM = getActivity().getPackageManager();
        Resources keyguardResources = null;
        try {
            keyguardResources = mPM.getResourcesForApplication("com.android.keyguard");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBatteryStatus = (CheckBoxPreference) prefs
                .findPreference(KEY_ALWAYS_BATTERY_PREF);
        if (mBatteryStatus != null) {
            mBatteryStatus.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0) == 1);
            mBatteryStatus.setOnPreferenceChangeListener(this);
        }

        mLockscreenRotation = (ListPreference) prefs
                .findPreference(KEY_LOCKSCREEN_ROTATION);
        if (mLockscreenRotation != null) {
            boolean defaultVal = !DeviceUtils.isPhone(getActivity());
            int userVal = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTATION_ENABLED, defaultVal ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mLockscreenRotation.setValue(String.valueOf(userVal));
            if (userVal == 0) {
                mLockscreenRotation.setSummary(mLockscreenRotation.getEntry());
            } else {
                mLockscreenRotation.setSummary(mLockscreenRotation.getEntry()
                        + " " + getResources().getString(
                        R.string.lockscreen_rotation_summary_extra));
            }
            mLockscreenRotation.setOnPreferenceChangeListener(this);
        }

        PreferenceCategory generalCategory = (PreferenceCategory)
                findPreference(KEY_GENERAL_CATEGORY);

        if (generalCategory != null) {
            Preference lockInterfacePref = findPreference(KEY_INTERFACE_SETTINGS);
            Preference lockTargetsPref = findPreference(KEY_TARGET_SETTINGS);
            if (lockInterfacePref != null && lockTargetsPref != null) {
                if (!DeviceUtils.isPhone(getActivity())) {
                     generalCategory.removePreference(lockInterfacePref);
                } else {
                     generalCategory.removePreference(lockTargetsPref);
                }
            }
        }

        mLockscreenWidgets = (Preference) findPreference(KEY_WIDGETS_SETTINGS);
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mLockscreenWidgets != null) {
            if (ActivityManager.isLowRamDeviceStatic()) {
                if (generalCategory != null) {
                    generalCategory.removePreference(prefs
                            .findPreference(KEY_WIDGETS_SETTINGS));
                    mLockscreenWidgets = null;
                }
            } else {
                final boolean disabled = (0 != (mDPM.getKeyguardDisabledFeatures(null)
                        & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL));
                mLockscreenWidgets.setEnabled(!disabled);
            }
        }

        PreferenceScreen lockButtons = (PreferenceScreen)
                 findPreference(KEY_LOCKSCREEN_BUTTONS);
        boolean hasButtons = (getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys) > 0);
        if (!hasButtons) {
            generalCategory.removePreference(lockButtons);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == mLockBeforeUnlock) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_BEFORE_UNLOCK,
                    ((Boolean) value) ? 1 : 0);
        } else if (preference == mBatteryStatus) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY,
                    ((Boolean) value) ? 1 : 0);
        } else if (preference == mLockscreenRotation) {
            int userVal = Integer.valueOf((String) value);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTATION_ENABLED,
                    userVal, UserHandle.USER_CURRENT);
            mLockscreenRotation.setValue(String.valueOf(value));
            if (userVal == 0) {
                mLockscreenRotation.setSummary(mLockscreenRotation.getEntry());
            } else {
                mLockscreenRotation.setSummary(mLockscreenRotation.getEntry()
                        + " " + getResources().getString(
                        R.string.lockscreen_rotation_summary_extra));
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockQuickUnlock) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    mLockQuickUnlock.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING,
                    mLockRingBattery.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
