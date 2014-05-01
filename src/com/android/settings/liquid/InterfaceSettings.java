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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class InterfaceSettings extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String TAG = "InterfaceSettings";
    private static final String KEY_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final String DENSITY_PROP = "persist.sys.lcd_density";
    private static final String KEY_HARDWARE_KEYS = "hardwarekeys_settings";

    private static final int DIALOG_CUSTOM_DENSITY = 101;

    private CheckBoxPreference mUseAltResolver;
    private static Preference mLcdDensity;

    private static int mMaxDensity = DisplayMetrics.getDeviceDensity();
    private static int mDefaultDensity = getLiquidDefaultDensity();
    private static int mMinDensity = getMinimumDensity();

    private static Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.liquid_interface_settings);

        mActivity = getActivity();

        updateSettings();
    }

    private void updateSettings() {
        mUseAltResolver = (CheckBoxPreference) findPreference(KEY_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0) == 1);
        mUseAltResolver.setOnPreferenceChangeListener(this);

        mLcdDensity = (Preference) findPreference(KEY_LCD_DENSITY);
        String current = SystemProperties.get(DENSITY_PROP,
                SystemProperties.get("ro.sf.lcd_density"));
        mLcdDensity.setSummary(getResources().getString(R.string.current_density) + current);
        mLcdDensity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDialogInner(DIALOG_CUSTOM_DENSITY);
                return true;
            }
        });

        PreferenceScreen hardwareKeys = (PreferenceScreen) findPreference(KEY_HARDWARE_KEYS);
        int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        if (deviceKeys == 0 && hardwareKeys != null) {
            getPreferenceScreen().removePreference(hardwareKeys);
        }
    }

    private static int getMinimumDensity() {
        int min = -1;
        int[] densities = { 91, 121, 161, 241, 321, 481 };
        for (int density : densities) {
            if (density < mMaxDensity) {
                min = density;
            }
        }
        return min;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
	
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
     }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUseAltResolver) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                    (Boolean) newValue ? 1 : 0);
            return true;
        }
        return false;
    }

    private static void setDensity(int density) {
        if (density < mMinDensity || density > mMaxDensity) {
            return;
        }
        SystemProperties.set(DENSITY_PROP, Integer.toString(density));
        final IWindowManager windowManagerService = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            windowManagerService.updateStatusBarNavBarHeight();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failure communicating with window manager", e);
        }
        Configuration configuration = new Configuration();
        configuration.setToDefaults();
        configuration.densityDpi = density;
        try {
            ActivityManagerNative.getDefault().updateConfiguration(configuration);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failure communicating with activity manager", e);
        }
        killRunningApps();
    }

    private static void killRunningApps() {
        ActivityManager am = (ActivityManager) mActivity.getSystemService(
                Context.ACTIVITY_SERVICE);
        String defaultKeyboard = Settings.Secure.getStringForUser(mActivity.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD, UserHandle.USER_CURRENT);
        am.forceStopPackage(defaultKeyboard);
        for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
            am.killBackgroundProcesses(pid.processName);
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        InterfaceSettings getOwner() {
            return (InterfaceSettings) getTargetFragment();
        }

        private void setTextDPI(TextView tv, String dpi) {
            tv.setText(getResources().getString(R.string.new_density) + dpi);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater factory = LayoutInflater.from(getActivity());
            int id = getArguments().getInt("id");
            switch (id) {
                case DIALOG_CUSTOM_DENSITY:
                    final View dialogView = factory.inflate(
                            R.layout.density_changer_dialog, null);
                    final TextView currentDPI = (TextView)
                            dialogView.findViewById(R.id.current_dpi);
                    final TextView newDPI = (TextView) dialogView.findViewById(R.id.new_dpi);
                    final SeekBar dpi = (SeekBar) dialogView.findViewById(R.id.dpi_edit);
                    String current = SystemProperties.get(DENSITY_PROP,
                            SystemProperties.get("ro.sf.lcd_density"));
                    setTextDPI(newDPI, current);
                    currentDPI.setText(getResources().getString(
                            R.string.current_density) + current);
                    dpi.setMax(mMaxDensity - mMinDensity);
                    dpi.setProgress(Integer.parseInt(current) - mMinDensity);
                    dpi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                int progress, boolean fromUser) {
                            int value = progress + mMinDensity;
                            setTextDPI(newDPI, Integer.toString(value));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    ImageView setDefault = (ImageView) dialogView.findViewById(R.id.default_dpi);
                    setDefault.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mDefaultDensity != -1) {
                                dpi.setProgress(mDefaultDensity - mMinDensity);
                                setTextDPI(newDPI, Integer.toString(mDefaultDensity));
                            }
                        }
                    });
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.set_custom_density_title))
                            .setView(dialogView)
                            .setPositiveButton(getResources().getString(
                                    R.string.set_custom_density_set),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    setDensity(dpi.getProgress() + mMinDensity);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }
    }

    public static int getLiquidDefaultDensity() {
        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("/system/build.prop");
            prop.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
            }
        }
        return Integer.parseInt(prop.getProperty(DENSITY_PROP, "-1"));
    }
}
