/*
 * Copyright (C) 2012 ParanoidAndroid Project
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
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieColor extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PA_PIE_ENABLE_COLOR = "pa_pie_enable_color";
    private static final String PA_PIE_JUICE = "pa_pie_juice";
    private static final String PA_PIE_BACKGROUND = "pa_pie_background";
    private static final String PA_PIE_SELECT = "pa_pie_select";
    private static final String PA_PIE_OUTLINES = "pa_pie_outlines";
    private static final String PA_PIE_STATUS_CLOCK = "pa_pie_status_clock";
    private static final String PA_PIE_STATUS = "pa_pie_status";
    private static final String PA_PIE_CHEVRON_LEFT = "pa_pie_chevron_left";
    private static final String PA_PIE_CHEVRON_RIGHT = "pa_pie_chevron_right";
    private static final String PA_PIE_BUTTON_COLOR = "pa_pie_button_color";

    CheckBoxPreference mEnableColor;
    ColorPickerPreference mPieBg;
    ColorPickerPreference mJuice;
    ColorPickerPreference mSelect;
    ColorPickerPreference mOutlines;
    ColorPickerPreference mStatusClock;
    ColorPickerPreference mStatus;
    ColorPickerPreference mChevronLeft;
    ColorPickerPreference mChevronRight;
    ColorPickerPreference mBtnColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pie_color);

        mEnableColor = (CheckBoxPreference) findPreference(PA_PIE_ENABLE_COLOR);
        mEnableColor.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PA_PIE_ENABLE_COLOR, 0) == 1);

        mPieBg = (ColorPickerPreference) findPreference(PA_PIE_BACKGROUND);
        mPieBg.setOnPreferenceChangeListener(this);

        mJuice = (ColorPickerPreference) findPreference(PA_PIE_JUICE);
        mJuice.setOnPreferenceChangeListener(this);

        mSelect = (ColorPickerPreference) findPreference(PA_PIE_SELECT);
        mSelect.setOnPreferenceChangeListener(this);

        mOutlines = (ColorPickerPreference) findPreference(PA_PIE_OUTLINES);
        mOutlines.setOnPreferenceChangeListener(this);

        mStatusClock = (ColorPickerPreference) findPreference(PA_PIE_STATUS_CLOCK);
        mStatusClock.setOnPreferenceChangeListener(this);

        mStatus = (ColorPickerPreference) findPreference(PA_PIE_STATUS);
        mStatus.setOnPreferenceChangeListener(this);

        mChevronLeft = (ColorPickerPreference) findPreference(PA_PIE_CHEVRON_LEFT);
        mChevronLeft.setOnPreferenceChangeListener(this);

        mChevronRight = (ColorPickerPreference) findPreference(PA_PIE_CHEVRON_RIGHT);
        mChevronRight.setOnPreferenceChangeListener(this);

        mBtnColor = (ColorPickerPreference) findPreference(PA_PIE_BUTTON_COLOR);
        mBtnColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableColor) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_ENABLE_COLOR,
                    mEnableColor.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieBg) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_BACKGROUND, intHex);
            return true;
        } else if (preference == mSelect) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_SELECT, intHex);
            return true;
        } else if (preference == mOutlines) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_OUTLINES, intHex);
            return true;
        } else if (preference == mStatusClock) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_STATUS_CLOCK, intHex);
            return true;
        } else if (preference == mStatus) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_STATUS, intHex);
            return true;
        } else if (preference == mChevronLeft) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_CHEVRON_LEFT, intHex);
            return true;
        } else if (preference == mChevronRight) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_CHEVRON_RIGHT, intHex);
            return true;
        } else if (preference == mBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mJuice) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_JUICE, intHex);
            return true;
        }
        return false;
    }
}
