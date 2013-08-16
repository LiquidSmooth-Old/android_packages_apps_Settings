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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.liquid.util.CMDProcessor;
import com.android.settings.liquid.util.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StartupTweaks extends SettingsPreferenceFragment {

    private static final String TAG = "StartupTweaks";

    private static final int MSG_LOAD_PREFS = 0;
    private static final int DIALOG_INIT_D_ERROR = 0;

    private static final String INIT_D = "/system/etc/init.d";
    private static final String INIT_D_CFG = "/system/etc/init.d.cfg";

    private static final String KEY_ZIPALIGN_APKS = "zipalign_apks";
    private static final String KEY_FIX_PERMISSIONS = "fix_permissions";
    private static final String KEY_ENABLE_SYSCTL = "enable_sysctl";
    private static final String KEY_CLEAR_DATA_CACHE = "clear_data_cache";
    private static final String KEY_ENABLE_CRON = "enable_cron";
    private static final String KEY_SD_BOOST = "sd_boost";
    private static final String KEY_FILE_SYSTEM_SPEEDUPS = "file_system_speedups";
    private static final String KEY_READ_AHEAD_KB = "read_ahead_kb";
    private static final String KEY_INIT_D_CREDITS = "init_d_credits";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_TOUCH = "touch";
    private static final String KEY_MINFREE = "minfree";
    private static final String KEY_GPURENDER = "gpurender";
    private static final String KEY_SLEEPERS = "sleepers";
    private static final String KEY_JOURNALISM = "journalism";
    private static final String KEY_SQLITE3 = "sqlite3";
    private static final String KEY_WIFISLEEP = "wifisleep";
    private static final String KEY_IOSTATS = "iostats";
    private static final String KEY_SENTRENICE = "sentrenice";
    private static final String KEY_TWEAKS = "tweaks";
    private static final String KEY_SPEEDY_MODIFIED = "speedy_modified";
    private static final String KEY_LOOPY_SMOOTHNESS_TWEAK = "loopy_smoothness_tweak";

    private static final String[] KEYS = {
        KEY_ZIPALIGN_APKS, KEY_FIX_PERMISSIONS, KEY_ENABLE_SYSCTL,
        KEY_CLEAR_DATA_CACHE, KEY_ENABLE_CRON, KEY_SD_BOOST,
                KEY_FILE_SYSTEM_SPEEDUPS, KEY_READ_AHEAD_KB, KEY_BATTERY,
        KEY_TOUCH, KEY_MINFREE, KEY_GPURENDER, KEY_SLEEPERS, KEY_JOURNALISM,
        KEY_SQLITE3, KEY_WIFISLEEP, KEY_IOSTATS, KEY_SENTRENICE, KEY_TWEAKS,
        KEY_SPEEDY_MODIFIED, KEY_LOOPY_SMOOTHNESS_TWEAK
    };

    private HashMap<String, String> mShellVariables;
    protected SharedPreferences mPrefs;
    private ProgressDialog mPbarDialog;

    private static StartupTweaks sActivity;

    public static StartupTweaks whatActivity() {
        return sActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sActivity = this;
        mPrefs    = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        loadValues();
    }

    private void loadValues() {
        mPbarDialog = new ProgressDialog(StartupTweaks.this.getActivity());
        mPbarDialog.setMessage("Loading values ...");
        mPbarDialog.show();
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mShellVariables = getShellVariables();
                mHandler.sendEmptyMessageDelayed(MSG_LOAD_PREFS, 1000);
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_LOAD_PREFS:
                mPbarDialog.dismiss();
                if (!isInitdSetup()) {
                    showDialog(DIALOG_INIT_D_ERROR);
                    return;
                }

                saveAllPrefs();
                addPreferencesFromResource(R.xml.startup_tweaks);
                mPrefs.registerOnSharedPreferenceChangeListener(
                        mOnSharedPreferenceChangeListener);
                break;
            }
        }
    };

    protected OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            Map<String, ?> prefs = mPrefs.getAll();
            String value = null;
            Object obj = prefs.get(key);
            if (obj instanceof Boolean) {
                value = (Boolean) obj ? "true" : "false";
            } else if (obj instanceof String) {
                value = (String) obj;
            } else if (obj instanceof Integer) {
                value = Integer.toString((Integer) obj);
            }
            if (value != null) {
                updateShellVariable(key, value);
            }
        }

    };

    private boolean isInitdSetup() {
        if (!new File(INIT_D_CFG).exists()) {
            Log.i(TAG, INIT_D_CFG + " does not exist!");
            return false;
        } else if (mShellVariables == null) {
            Log.i(TAG, "Failed getting shell variables!");
            return false;
        } else if (!new File(INIT_D).isDirectory()) {
            Log.i(TAG, INIT_D + " does not exist!");
            return false;
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(final int id) {
        if (id == DIALOG_INIT_D_ERROR) {
            return new AlertDialog.Builder(StartupTweaks.this.getActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.dt_tweaks_error))
            .setCancelable(false)
            .setMessage(getString(R.string.dm_tweaks_error))
            .setPositiveButton(getString(R.string.db_exit),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            })
            .create();
        } else {
            return null;
        }
    };

    private boolean saveAllPrefs() {
        SharedPreferences.Editor editor = mPrefs.edit();
        for (int i = 0; i < KEYS.length; i++) {
            String key = KEYS[i];
            String value = mShellVariables.get(key);
            boolean isBool = value.equals("true") || value.equals("false");
            if (isBool) {
                editor.putBoolean(key, value.equals("true"));
            } else {
                editor.putString(key, value);
            }
        }
        return editor.commit();
    }

    private void updateShellVariable(String key, String value) {
        CMDProcessor.SH shell = new CMDProcessor().su;
        shell.runWaitFor("busybox mount -o remount,rw /system");
        shell.runWaitFor("busybox sed -i 's|" + key + "=.*|" + key + "=" + value + "|' " + INIT_D_CFG);
        shell.runWaitFor("busybox mount -o remount,ro /system");
        mShellVariables.put(key, value);
    }

    private HashMap<String, String> getShellVariables() {
        HashMap<String, String> variables = null;
        CMDProcessor.SH sh = new CMDProcessor().sh;
        CMDProcessor.CommandResult result;
        int numOfKeys = KEYS.length;
        String[] cmds = new String[numOfKeys + 1];
        String[] values;
        int numOfValues;

        cmds[0] = ". " + INIT_D_CFG;
        for (int i = 0; i < numOfKeys; i++) {
            cmds[i + 1] = "echo $" + KEYS[i];
        }

        result = sh.runWaitFor(cmds);
        values = result.stdout.split("[\r\n]+");
        numOfValues = values.length;
        if (numOfValues == numOfKeys) {
            variables = new HashMap<String, String>();
            for (int i = 0; i < numOfKeys; i++) {
                variables.put(KEYS[i], values[i]);
            }
        }

        if (variables == null) {
            variables = new HashMap<String, String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(INIT_D_CFG), 256);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("=")) {
                        String value = line.substring(line.lastIndexOf("=") + 1);
                        for (String name : KEYS) {
                            if (line.startsWith(name + "=")) {
                                variables.put(name, value);
                                break;
                            }
                        }
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, INIT_D_CFG + " does not exist");
                variables = null;
            } catch (IOException e) {
                Log.d(TAG, "Error reading " + INIT_D_CFG);
                variables = null;
            }
        }
        return variables;
    }

}