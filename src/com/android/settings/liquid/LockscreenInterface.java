/*
 * Copyright (C) 2013 Slimroms
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.util.liquid.DeviceUtils;

import java.io.File;
import java.io.IOException;

public class LockscreenInterface extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenInterface";

    private static final int DLG_ENABLE_EIGHT_TARGETS = 0;
    private static final int REQUEST_CODE_BG_WALLPAPER = 1024;

    private static final String LOCKSCREEN_GENERAL_CATEGORY = "general_category";
    private static final String PREF_LOCKSCREEN_EIGHT_TARGETS = "lockscreen_eight_targets";
    private static final String PREF_LOCKSCREEN_SHORTCUTS = "lockscreen_shortcuts";
    private static final String PREF_LOCKSCREEN_TORCH = "lockscreen_torch";
    private static final String KEY_LOCKSCREEN_WALLPAPER = "lockscreen_wallpaper";
    private static final String KEY_SELECT_LOCKSCREEN_WALLPAPER = "select_lockscreen_wallpaper";

    private CheckBoxPreference mLockscreenEightTargets;
    private CheckBoxPreference mGlowpadTorch;
    private CheckBoxPreference mLockscreenWallpaper;
    private Preference mSelectLockscreenWallpaper;
    private Preference mShortcuts;

    private boolean mCheckPreferences;

    private File mWallpaperTemporary;

    private IKeyguardService mKeyguardService;

    private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mKeyguardService = IKeyguardService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mKeyguardService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createCustomLockscreenView();
    }

    private PreferenceScreen createCustomLockscreenView() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.lockscreen_interface);
        prefs = getPreferenceScreen();

        mLockscreenEightTargets = (CheckBoxPreference) findPreference(
                PREF_LOCKSCREEN_EIGHT_TARGETS);
        mLockscreenEightTargets.setChecked(Settings.System.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_EIGHT_TARGETS, 0) == 1);
        mLockscreenEightTargets.setOnPreferenceChangeListener(this);

        mGlowpadTorch = (CheckBoxPreference) findPreference(
                PREF_LOCKSCREEN_TORCH);
        mGlowpadTorch.setChecked(Settings.System.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_GLOWPAD_TORCH, 0) == 1);
        mGlowpadTorch.setOnPreferenceChangeListener(this);

        if (!DeviceUtils.deviceSupportsTorch(getActivity().getApplicationContext())) {
            prefs.removePreference(mGlowpadTorch);
        }

        mShortcuts = (Preference) findPreference(PREF_LOCKSCREEN_SHORTCUTS);
        mShortcuts.setEnabled(!mLockscreenEightTargets.isChecked());

        Intent intent = new Intent();
        intent.setClassName("com.android.keyguard", "com.android.keyguard.KeyguardService");
        if (!mContext.bindServiceAsUser(intent, mKeyguardConnection,
                Context.BIND_AUTO_CREATE, UserHandle.OWNER)) {
            Log.e(TAG, "*** Keyguard: can't bind to keyguard");
        }

        mLockscreenWallpaper = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_WALLPAPER);
        mLockscreenWallpaper.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_WALLPAPER, 0) == 1);

        mSelectLockscreenWallpaper = findPreference(KEY_SELECT_LOCKSCREEN_WALLPAPER);
        mSelectLockscreenWallpaper.setEnabled(mLockscreenWallpaper.isChecked());
        mWallpaperTemporary = new File(getActivity().getCacheDir() + "/lockwallpaper.tmp");

        mCheckPreferences = true;
        return prefs;
    }

    @Override
    public void onResume() {
        super.onResume();
        createCustomLockscreenView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mLockscreenEightTargets) {
            showDialogInner(DLG_ENABLE_EIGHT_TARGETS, (Boolean) objValue);
            return true;
        } else if (preference == mGlowpadTorch) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_GLOWPAD_TORCH,
                    (Boolean) objValue ? 1 : 0);
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode,
            Intent imageReturnedIntent) {
        if (requestCode == REQUEST_CODE_BG_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (mWallpaperTemporary.length() == 0 || !mWallpaperTemporary.exists()) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.shortcut_image_not_valid),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeFile(mWallpaperTemporary.getAbsolutePath());
                try {
                    mKeyguardService.setWallpaper(bmp);
                    Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 0);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to set wallpaper: " + ex);
                }
            }
        }
        if (mWallpaperTemporary.exists()) mWallpaperTemporary.delete();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockscreenWallpaper) {
            if (!mLockscreenWallpaper.isChecked()) setWallpaper(null);
            else Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_WALLPAPER, 1);
            mSelectLockscreenWallpaper.setEnabled(mLockscreenWallpaper.isChecked());
        } else if (preference == mSelectLockscreenWallpaper) {
            final Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            final Display display = getActivity().getWindowManager().getDefaultDisplay();

            boolean isPortrait = getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT;

            Point size = new Point();
            display.getSize(size);

            intent.putExtra("aspectX", isPortrait ? size.x : size.y);
            intent.putExtra("aspectY", isPortrait ? size.y : size.x);

            try {
                mWallpaperTemporary.createNewFile();
                mWallpaperTemporary.setWritable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mWallpaperTemporary));
                getActivity().startActivityFromFragment(this, intent, REQUEST_CODE_BG_WALLPAPER);
            } catch (IOException e) {
                // Do nothing here
            } catch (ActivityNotFoundException e) {
                // Do nothing here
            }
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    private void setWallpaper(Bitmap bmp) {
        try {
            mKeyguardService.setWallpaper(bmp);
        } catch (RemoteException ex) {
            Log.e(TAG, "Unable to set wallpaper!");
        }
    }

    private void showDialogInner(int id, boolean state) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, state);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id, boolean state) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putBoolean("state", state);
            frag.setArguments(args);
            return frag;
        }

        LockscreenInterface getOwner() {
            return (LockscreenInterface) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            final boolean state = getArguments().getBoolean("state");
            switch (id) {
                case DLG_ENABLE_EIGHT_TARGETS:
                    String message = getOwner().getResources()
                                .getString(R.string.lockscreen_enable_eight_targets_dialog);
                    if (state) {
                        message = message + " " + getOwner().getResources().getString(
                                R.string.lockscreen_enable_eight_targets_enabled_dialog);
                    }
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(message)
                    .setNegativeButton(R.string.dlg_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().getContentResolver(),
                                    Settings.System.LOCKSCREEN_EIGHT_TARGETS, state ? 1 : 0);
                            getOwner().mShortcuts.setEnabled(!state);
                            Settings.System.putString(getOwner().getContentResolver(),
                                    Settings.System.LOCKSCREEN_TARGETS, null);
                            for (File pic : getOwner().getActivity().getFilesDir().listFiles()) {
                                if (pic.getName().startsWith("lockscreen_")) {
                                    pic.delete();
                                }
                            }
                            if (state) {
                                Toast.makeText(getOwner().getActivity(),
                                        R.string.lockscreen_target_reset,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            int id = getArguments().getInt("id");
            boolean state = getArguments().getBoolean("state");
            switch (id) {
                case DLG_ENABLE_EIGHT_TARGETS:
                    getOwner().mLockscreenEightTargets.setChecked(!state);
                    break;
             }
        }
    }
}
