/*
 * Copyright (C) 2014 The LiquidSmooth Project
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.settings.liquid.util.CMDProcessor;
import com.android.settings.liquid.util.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PropModder extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PropModder";

    private static final String APPEND_CMD = "echo \"%s=%s\" >> /system/build.prop";
    private static final String KILL_PROP_CMD = "busybox sed -i \"/%s/D\" /system/build.prop";
    private static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %s=%s\" /system/build.prop";
    private static final String LOGCAT_CMD = "busybox sed -i \"/log/ c %s\" /system/etc/init.d/72propmodder_script";
    private static final String FIND_CMD = "grep -q \"%s\" /system/build.prop";
    private static final String REMOUNT_CMD = "mount -o remount,%s /system";
    private static final String PROP_EXISTS_CMD = "grep -q %s /system/build.prop";
    private static final String DISABLE = "disable";
    private static final String SHOWBUILD_PATH = "/cache/showbuild";
    private static final String INIT_SCRIPT_TEMP_PATH = "/cache/init_script";
    private static final String WIFI_SCAN_PREF = "pref_wifi_scan_interval";
    private static final String WIFI_SCAN_PROP = "wifi.supplicant_scan_interval";
    private static final String WIFI_SCAN_PERSIST_PROP = "persist.wifi_scan_interval";
    private static final String WIFI_SCAN_DEFAULT = System.getProperty(WIFI_SCAN_PROP);
    private static final String MAX_EVENTS_PREF = "pref_max_events";
    private static final String MAX_EVENTS_PROP = "windowsmgr.max_events_per_sec";
    private static final String MAX_EVENTS_PERSIST_PROP = "persist.max_events";
    private static final String MAX_EVENTS_DEFAULT = System.getProperty(MAX_EVENTS_PROP);
    private static final String USB_MODE_PREF = "pref_usb_mode";
    private static final String USB_MODE_PROP = "ro.default_usb_mode";
    private static final String USB_MODE_PERSIST_PROP = "persist.usb_mode";
    private static final String USB_MODE_DEFAULT = System.getProperty(USB_MODE_PROP);
    private static final String RING_DELAY_PREF = "pref_ring_delay";
    private static final String RING_DELAY_PROP = "ro.telephony.call_ring.delay";
    private static final String RING_DELAY_PERSIST_PROP = "persist.call_ring.delay";
    private static final String RING_DELAY_DEFAULT = System.getProperty(RING_DELAY_PROP);
    private static final String VM_HEAPSIZE_PREF = "pref_vm_heapsize";
    private static final String VM_HEAPSIZE_PROP = "dalvik.vm.heapsize";
    private static final String VM_HEAPSIZE_PERSIST_PROP = "persist.vm_heapsize";
    private static final String VM_HEAPSIZE_DEFAULT = System.getProperty(VM_HEAPSIZE_PROP);
    private static final String FAST_UP_PREF = "pref_fast_up";
    private static final String FAST_UP_PROP = "ro.ril.hsxpa";
    private static final String FAST_UP_PERSIST_PROP = "persist.fast_up";
    private static final String FAST_UP_DEFAULT = System.getProperty(FAST_UP_PROP);
    private static final String PROX_DELAY_PREF = "pref_prox_delay";
    private static final String PROX_DELAY_PROP = "mot.proximity.delay";
    private static final String PROX_DELAY_PERSIST_PROP = "persist.prox.delay";
    private static final String PROX_DELAY_DEFAULT = System.getProperty(PROX_DELAY_PROP);
    private static final String MOD_VERSION_PREF = "pref_mod_version";
    private static final String MOD_VERSION_PROP = "ro.build.display.id";
    private static final String MOD_VERSION_PERSIST_PROP = "persist.build.display.id";
    private static final String MOD_VERSION_DEFAULT = System.getProperty(MOD_VERSION_PROP);
    private static final String MOD_BUTTON_TEXT = "doMod";
    private static final String MOD_VERSION_TEXT = "Mods by PropModder";
    private static final String SLEEP_PREF = "pref_sleep";
    private static final String SLEEP_PROP = "pm.sleep_mode";
    private static final String SLEEP_PERSIST_PROP = "persist.sleep";
    private static final String SLEEP_DEFAULT = System.getProperty(SLEEP_PROP);
    private static final String TCP_STACK_PREF = "pref_tcp_stack";
    private static final String TCP_STACK_PERSIST_PROP = "persist_tcp_stack";
    private static final String TCP_STACK_PROP_0 = "net.tcp.buffersize.default";
    private static final String TCP_STACK_PROP_1 = "net.tcp.buffersize.wifi";
    private static final String TCP_STACK_PROP_2 = "net.tcp.buffersize.umts";
    private static final String TCP_STACK_PROP_3 = "net.tcp.buffersize.gprs";
    private static final String TCP_STACK_PROP_4 = "net.tcp.buffersize.edge";
    private static final String TCP_STACK_BUFFER = "4096,87380,256960,4096,16384,256960";
    private static final String THREE_G_PREF = "pref_g_speed";
    private static final String THREE_G_PERSIST_PROP = "persist_3g_speed";
    private static final String THREE_G_PROP_0 = "ro.ril.enable.3g.prefix";
    private static final String THREE_G_PROP_1 = "ro.ril.hep";
    private static final String THREE_G_PROP_2 = FAST_UP_PROP;
    private static final String THREE_G_PROP_3 = "ro.ril.enable.dtm";
    private static final String THREE_G_PROP_4 = "ro.ril.gprsclass";
    private static final String THREE_G_PROP_5 = "ro.ril.hsdpa.category";
    private static final String THREE_G_PROP_6 = "ro.ril.enable.a53";
    private static final String THREE_G_PROP_7 = "ro.ril.hsupa.category";
    private static final String GPU_PREF = "pref_gpu";
    private static final String GPU_PERSIST_PROP = "persist_gpu";
    private static final String GPU_PROP = "debug.sf.hw";
    private static final String DISABLE_BOOTANIMATION_PREF = "pref_disable_bootanimation";
    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "debug.sf.nobootanimation";
    private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";

    private String placeholder;
    private String tcpstack0;

    private String ModPrefHolder = SystemProperties.get(MOD_VERSION_PERSIST_PROP,
                SystemProperties.get(MOD_VERSION_PROP, MOD_VERSION_DEFAULT));

    //handles for our menu hard key press
    private final int MENU_MARKET = 1;
    private final int MENU_REBOOT = 2;
    private int NOTE_ID;

    private ListPreference mWifiScanPref;
    private ListPreference mMaxEventsPref;
    private ListPreference mRingDelayPref;
    private ListPreference mVmHeapsizePref;
    private ListPreference mFastUpPref;
    private ListPreference mProxDelayPref;
    private EditTextPreference mModVersionPref;
    private ListPreference mSleepPref;
    private CheckBoxPreference mTcpStackPref;
    private CheckBoxPreference m3gSpeedPref;
    private CheckBoxPreference mGpuPref;
    private AlertDialog mAlertDialog;
    private NotificationManager mNotificationManager;
    private CheckBoxPreference mDisableBootanimPref;

    private File tmpDir = new File("/cache");
    private File init_d = new File("/system/etc/init.d");

    //handler for command processor
    private final CMDProcessor cmd = new CMDProcessor();
    private PreferenceScreen prefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prop_modder);
        prefSet = getPreferenceScreen();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_liquid_theme_settings);

        mWifiScanPref = (ListPreference) prefSet.findPreference(WIFI_SCAN_PREF);
        mWifiScanPref.setOnPreferenceChangeListener(this);

        mMaxEventsPref = (ListPreference) prefSet.findPreference(MAX_EVENTS_PREF);
        mMaxEventsPref.setOnPreferenceChangeListener(this);

        mRingDelayPref = (ListPreference) prefSet.findPreference(RING_DELAY_PREF);
        mRingDelayPref.setOnPreferenceChangeListener(this);

        mVmHeapsizePref = (ListPreference) prefSet.findPreference(VM_HEAPSIZE_PREF);
        mVmHeapsizePref.setOnPreferenceChangeListener(this);

        mFastUpPref = (ListPreference) prefSet.findPreference(FAST_UP_PREF);
        mFastUpPref.setOnPreferenceChangeListener(this);

        mProxDelayPref = (ListPreference) prefSet.findPreference(PROX_DELAY_PREF);
        mProxDelayPref.setOnPreferenceChangeListener(this);

        mSleepPref = (ListPreference) prefSet.findPreference(SLEEP_PREF);
        mSleepPref.setOnPreferenceChangeListener(this);

        mTcpStackPref = (CheckBoxPreference) prefSet.findPreference(TCP_STACK_PREF);

        mModVersionPref = (EditTextPreference) prefSet.findPreference(MOD_VERSION_PREF);
        String mod = Helpers.findBuildPropValueOf(MOD_VERSION_PROP);
        if (mModVersionPref != null) {
            EditText modET = mModVersionPref.getEditText();
            ModPrefHolder = mModVersionPref.getEditText().toString();
            if (modET != null){
                InputFilter lengthFilter = new InputFilter.LengthFilter(32);
                modET.setFilters(new InputFilter[]{lengthFilter});
                modET.setSingleLine(true);
            }
            mModVersionPref.setSummary(String.format(getString(R.string.pref_mod_version_alt_summary), mod));
        }
        Log.d(TAG, String.format("ModPrefHoler = '%s' found build number = '%s'", ModPrefHolder, mod));
        mModVersionPref.setOnPreferenceChangeListener(this);

        m3gSpeedPref = (CheckBoxPreference) prefSet.findPreference(THREE_G_PREF);

        mDisableBootanimPref = (CheckBoxPreference) prefSet.findPreference(DISABLE_BOOTANIMATION_PREF);

        mGpuPref = (CheckBoxPreference) prefSet.findPreference(GPU_PREF);

        updateScreen();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /* handle CheckBoxPreference clicks */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mTcpStackPref) {
            Log.d(TAG, "mTcpStackPref.onPreferenceTreeClick()");
            value = mTcpStackPref.isChecked();
            return doMod(null, TCP_STACK_PROP_0, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_1, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_2, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_3, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PERSIST_PROP, TCP_STACK_PROP_4, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE));
        } else if (preference == mDisableBootanimPref) {
            value = mDisableBootanimPref.isChecked();
            return doMod(DISABLE_BOOTANIMATION_PREF, DISABLE_BOOTANIMATION_PERSIST_PROP, String.valueOf(value ? 1 : DISABLE));
        } else if (preference == m3gSpeedPref) {
            value = m3gSpeedPref.isChecked();
            return doMod(THREE_G_PERSIST_PROP, THREE_G_PROP_0, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_1, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_2, String.valueOf(value ? 2 : DISABLE))
                && doMod(null, THREE_G_PROP_3, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_4, String.valueOf(value ? 12 : DISABLE))
                && doMod(null, THREE_G_PROP_5, String.valueOf(value ? 8 : DISABLE))
                && doMod(null, THREE_G_PROP_6, String.valueOf(value ? 1 : DISABLE))
                && doMod(null, THREE_G_PROP_7, String.valueOf(value ? 5 : DISABLE));
        } else if (preference == mGpuPref) {
            value = mGpuPref.isChecked();
            return doMod(GPU_PERSIST_PROP, GPU_PROP, String.valueOf(value ? 1 : DISABLE));
        }

        return false;
    }

    /* handle ListPreferences and EditTextPreferences */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            Log.e(TAG, "New preference selected: " + newValue);
            if (preference == mWifiScanPref) {
                return doMod(WIFI_SCAN_PERSIST_PROP, WIFI_SCAN_PROP,
                        newValue.toString());
            } else if (preference == mMaxEventsPref) {
                return doMod(MAX_EVENTS_PERSIST_PROP, MAX_EVENTS_PROP,
                        newValue.toString());
            } else if (preference == mRingDelayPref) {
                return doMod(RING_DELAY_PERSIST_PROP, RING_DELAY_PROP,
                        newValue.toString());
            } else if (preference == mVmHeapsizePref) {
                return doMod(VM_HEAPSIZE_PERSIST_PROP, VM_HEAPSIZE_PROP,
                        newValue.toString());
            } else if (preference == mFastUpPref) {
                return doMod(FAST_UP_PERSIST_PROP, FAST_UP_PROP,
                        newValue.toString());
            } else if (preference == mProxDelayPref) {
                 return doMod(PROX_DELAY_PERSIST_PROP, PROX_DELAY_PROP,
                        newValue.toString());
            } else if (preference == mModVersionPref) {
                 return doMod(MOD_VERSION_PERSIST_PROP, MOD_VERSION_PROP,
                        newValue.toString());
            } else if (preference == mSleepPref) {
                 return doMod(SLEEP_PERSIST_PROP, SLEEP_PROP,
                        newValue.toString());
            }
        }

        return false;
    }

    /* method to handle mods */
    private boolean doMod(String persist, String key, String value) {

        if (persist != null) {
            SystemProperties.set(persist, value);
        }
        Log.d(TAG, String.format("Calling script with args '%s' and '%s'", key, value));
        backupBuildProp();
        if (!mount("rw")) {
            throw new RuntimeException("Could not remount /system rw");
        }
        boolean success = true;
        try {
            if (!propExists(key) && value.equals(DISABLE)) {
                Log.d(TAG, String.format("We want {%s} DISABLED however it doesn't exist so we do nothing and move on", key));
            } else if (propExists(key)) {
                if (value.equals(DISABLE)) {
                    Log.d(TAG, String.format("value == %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(KILL_PROP_CMD, key)).success();
                } else {
                    Log.d(TAG, String.format("value != %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(REPLACE_CMD, key, key, value)).success();
                }
            } else {
                Log.d(TAG, "append command starting");
                success = cmd.su.runWaitFor(String.format(APPEND_CMD, key, value)).success();
            }
            if (success) {
                restoreBuildProp();
            } else {
                updateScreen();
            }
        } finally {
            mount("ro");
        }

        return success;
    }

    public boolean mount(String read_value) {
        Log.d(TAG, "Remounting /system " + read_value);
        return cmd.su.runWaitFor(String.format(REMOUNT_CMD, read_value)).success();
    }

    public boolean propExists(String prop) {
        Log.d(TAG, "Checking if prop " + prop + " exists in /system/build.prop");
        return cmd.su.runWaitFor(String.format(PROP_EXISTS_CMD, prop)).success();
    }

    public void updateShowBuild() {
        Log.d(TAG, "Setting up /cache/showbuild");
        try {
            mount("rw");
            cmd.su.runWaitFor("cp -f /system/build.prop " + SHOWBUILD_PATH).success();
            cmd.su.runWaitFor("chmod 777 " + SHOWBUILD_PATH).success();
        } finally {
            mount("ro");
        }
    }

    public boolean backupBuildProp() {
        Log.d(TAG, "Backing up build.prop to /cache/pm_build.prop");
        return cmd.su.runWaitFor("cp /system/build.prop /cache/pm_build.prop").success();
    }

    public boolean restoreBuildProp() {
        Log.d(TAG, "Restoring build.prop from /cache/pm_build.prop");
        return cmd.su.runWaitFor("cp /cache/pm_build.prop /cache/build.prop").success();
    }

    public void updateScreen() {
        //update all the summaries
        String wifi = Helpers.findBuildPropValueOf(WIFI_SCAN_PROP);
        if (!wifi.equals(DISABLE)) {
            mWifiScanPref.setValue(wifi);
            mWifiScanPref.setSummary(String.format(getString(R.string.pref_wifi_scan_alt_summary), wifi));
        } else {
            mWifiScanPref.setValue(WIFI_SCAN_DEFAULT);
        }
        String maxE = Helpers.findBuildPropValueOf(MAX_EVENTS_PROP);
        if (!maxE.equals(DISABLE)) {
            mMaxEventsPref.setValue(maxE);
            mMaxEventsPref.setSummary(String.format(getString(R.string.pref_max_events_alt_summary), maxE));
        } else {
            mMaxEventsPref.setValue(MAX_EVENTS_DEFAULT);
        }
        String ring = Helpers.findBuildPropValueOf(RING_DELAY_PROP);
        if (!ring.equals(DISABLE)) {
            mRingDelayPref.setValue(ring);
            mRingDelayPref.setSummary(String.format(getString(R.string.pref_ring_delay_alt_summary), ring));
        } else {
            mRingDelayPref.setValue(RING_DELAY_DEFAULT);
        }
        String vm = Helpers.findBuildPropValueOf(VM_HEAPSIZE_PROP);
        if (!vm.equals(DISABLE)) {
            mVmHeapsizePref.setValue(vm);
            mVmHeapsizePref.setSummary(String.format(getString(R.string.pref_vm_heapsize_alt_summary), vm));
        } else {
            mVmHeapsizePref.setValue(VM_HEAPSIZE_DEFAULT);
        }
        String fast = Helpers.findBuildPropValueOf(FAST_UP_PROP);
        if (!fast.equals(DISABLE)) {
            mFastUpPref.setValue(fast);
            mFastUpPref.setSummary(String.format(getString(R.string.pref_fast_up_alt_summary), fast));
        } else {
            mFastUpPref.setValue(FAST_UP_DEFAULT);
        }
        String prox = Helpers.findBuildPropValueOf(PROX_DELAY_PROP);
        if (!prox.equals(DISABLE)) {
            mProxDelayPref.setValue(prox);
            mProxDelayPref.setSummary(String.format(getString(R.string.pref_prox_delay_alt_summary), prox));
        } else {
            mProxDelayPref.setValue(PROX_DELAY_DEFAULT);
        }
        String sleep = Helpers.findBuildPropValueOf(SLEEP_PROP);
        if (!sleep.equals(DISABLE)) {
            mSleepPref.setValue(sleep);
            mSleepPref.setSummary(String.format(getString(R.string.pref_sleep_alt_summary), sleep));
        } else {
            mSleepPref.setValue(SLEEP_DEFAULT);
        }
        String tcp = Helpers.findBuildPropValueOf(TCP_STACK_PROP_0);
        if (tcp.equals(TCP_STACK_BUFFER)) {
            mTcpStackPref.setChecked(true);
        } else {
            mTcpStackPref.setChecked(false);
        }
        String mod = Helpers.findBuildPropValueOf(MOD_VERSION_PROP);
        mModVersionPref.setSummary(String.format(getString(R.string.pref_mod_version_alt_summary), mod));
        String g0 = Helpers.findBuildPropValueOf(THREE_G_PROP_0);
        String g3 = Helpers.findBuildPropValueOf(THREE_G_PROP_3);
        String g6 = Helpers.findBuildPropValueOf(THREE_G_PROP_6);
        if (g0.equals("1") && g3.equals("1") && g6.equals("1")) {
            m3gSpeedPref.setChecked(true);
        } else {
            m3gSpeedPref.setChecked(false);
        }
        String gpu = Helpers.findBuildPropValueOf(GPU_PROP);
        if (!gpu.equals(DISABLE)) {
            mGpuPref.setChecked(true);
        } else {
            mGpuPref.setChecked(false);
        }
    }
}
