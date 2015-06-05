/*
 * Copyright (C) 2015 STELIX Carbon project
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

package com.carbon.fibers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

// fragments import for entry
import com.android.settings.*;
import com.android.settings.bliss.*;
import com.android.settings.bliss.activities.*;
import com.android.settings.bliss.navbar.*;
import com.android.settings.carbon.*;
import com.android.settings.carbon.gestureanywhere.*;
import com.android.settings.cyanogenmod.*;
import com.android.settings.cyanogenmod.qs.*;
import com.android.settings.slim.*;
import com.android.settings.slim.dslv.*;
import com.carbon.fibers.fragments.*;

public class CarbonSettingsActivity extends PreferenceActivity implements ButtonBarHandler {

    private static final String TAG = "CR_Settings";

    private static String KEY_USE_ENGLISH_LOCALE = "use_english_locale";

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();
    private List<Header> mHeaders;

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    boolean mInLocalHeaderSwitch;

    Locale defaultLocale;

    protected boolean isShortcut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        defaultLocale = Locale.getDefault();
        Log.i(TAG, "defualt locale: " + defaultLocale.getDisplayName());
        setLocale();

        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader();
            // Force the title so that it doesn't get overridden by a direct
            // launch of
            // a specific settings screen.
            setTitle(R.string.carbon_fibers_title);
        }

        if ("com.carbon.fibers.START_NEW_FRAGMENT".equals(getIntent().getAction())) {
            String className = getIntent().getStringExtra("carbon_fragment_name").toString();
            Class<?> cls = null;
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                // can't find the class at all, die
                return;
            }

            try {
                cls.asSubclass(SettingsActivity.class);
                return;
            } catch (ClassCastException e) {
                // fall through
            }

            try {
                cls.asSubclass(Fragment.class);
                Bundle b = new Bundle();
                b.putBoolean("started_from_shortcut", true);
                isShortcut = true;
                startWithFragment(className, b, null, 0);
                finish(); // close current activity
                return;
            } catch (ClassCastException e) {
            }

            try {
                cls.asSubclass(Activity.class);
                isShortcut = true;
                Intent activity = new Intent(getApplicationContext(), cls);
                activity.putExtra("started_from_shortcut", true);
                startActivity(activity);
                finish(); // close current activity
                return;
            } catch (ClassCastException e) {
            }
        }
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (isShortcut) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        MenuItem locale = menu.findItem(R.id.change_locale);

        if (Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            menu.removeItem(R.id.change_locale);
        } else {
            Configuration config = getBaseContext().getResources().getConfiguration();
            locale.setTitle("Locale (" + config.locale.getDisplayLanguage() + ")");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.change_locale:
                Log.e(TAG, "change_locale clicked");
                SharedPreferences p = getPreferences(MODE_PRIVATE);
                boolean useEnglishLocale = p.getBoolean(KEY_USE_ENGLISH_LOCALE, false);
                p.edit().putBoolean(KEY_USE_ENGLISH_LOCALE, !useEnglishLocale).apply();
                recreate();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setLocale() {
        SharedPreferences p = getPreferences(MODE_PRIVATE);
        boolean useEnglishLocale = p.getBoolean(KEY_USE_ENGLISH_LOCALE, false);

        if (useEnglishLocale) {
            Locale locale = null;
            Configuration config = null;
            config = getBaseContext().getResources().getConfiguration();
            locale = Locale.ENGLISH;
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            Locale locale = null;
            Configuration config = null;
            config = getBaseContext().getResources().getConfiguration();
            locale = defaultLocale;
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

        }
    }

    private static final String[] ENTRY_FRAGMENTS = {
        ActionListViewSettings.class.getName(),
        AnimationControls.class.getName(),
        AppCircleBar.class.getName(),
        AppSidebar.class.getName(),
        ArrangeNavbarFragment.class.getName(),
        BaseSetting.class.getName(),
        BacklightTimeoutSeekBar.class.getName(),
        ButtonBacklightBrightness.class.getName(),
        ButtonSettings.class.getName(),
        CarbonButtons.class.getName(),
        CarbonInterface.class.getName(),
        CarbonMoreDeviceSettings.class.getName(),
        CarbonNavBar.class.getName(),
        CarbonNotificationDrawerSettings.class.getName(),
        CarbonRecentsSettings.class.getName(),
        CarbonStatusBar.class.getName(),
        CarrierLabel.class.getName(),
        DeviceUtils.class.getName(),
        DisplayAnimationsSettings.class.getName(),
        GestureAnywhereSettings.class.getName(),
        NavBarSettings.class.getName(),
        NavbarSettingsFragment.class.getName(),
        NavbarTabHostFragment.class.getName(),
        NavRing.class.getName(),
        NetworkTraffic.class.getName(),
        NetworkTrafficFragment.class.getName(),
        NotificationDrawerSettings.class.getName(),
        PieButtonFragment.class.getName(),
        PieButtonStyleSettings.class.getName(),
        PieControl.class.getName(),
        PieStyleSettings.class.getName(),
        PieTriggerSettings.class.getName(),
        PowerMenuActions.class.getName(),
        QSColors.class.getName(),
        QSTiles.class.getName(),
        StatusBarBatteryStatusSettings.class.getName(),
        StatusBarClockStyle.class.getName(),
        StatusBarExpandedHeaderSettings.class.getName(),
        StatusBarNotifSystemIconsSettings.class.getName(),
        StatusBarSettings.class.getName(),
        StatusBarSignalSettings.class.getName(),
        WakeLockBlocker.class.getName()
    };

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // Almost all fragments are wrapped in this,
        // except for a few that have their own activities.
        for (int i = 0; i < ENTRY_FRAGMENTS.length; i++) {
            if (ENTRY_FRAGMENTS[i].equals(fragmentName)) return true;
        }
        return false;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {
        loadHeadersFromResource(R.xml.carbon_preference_headers, headers);

        updateHeaderList(headers);

        mHeaders = headers;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be
     * launched for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    private void highlightHeader() {
        if (mTopLevelHeaderId != 0) {
            Integer index = mHeaderIndexMap.get(mTopLevelHeaderId);
            if (index != null) {
                getListView().setItemChecked(index, true);
            }
        }
    }

    private void updateHeaderList(List<Header> target) {
        int i = 0;
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int) header.id;

            // Increment if the current one wasn't removed by the Utils code.
            if (target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the
                // top-level
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setLocale();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }
    }

    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null)
            return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName()))
            return null;

        return intentClass;
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (mHeaders == null) {
            mHeaders = new ArrayList<Header>();
            // When the saved state provides the list of headers, onBuildHeaders
            // is not called
            // Copy the list of Headers from the adapter, preserving their order
            for (int i = 0; i < adapter.getCount(); i++) {
                mHeaders.add((Header) adapter.getItem(i));
            }
        }

        // Ignore the adapter provided by PreferenceActivity and substitute ours
        // instead
        super.setListAdapter(new HeaderAdapter(this, mHeaders));
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switch_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Temp Switches provided as placeholder until the adapter replaces
            // these with actual
            // Switches inflated from their layouts. Must be done before adapter
            // is set in super
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                com.android.internal.R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(com.android.internal.R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(com.android.internal.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may
            // be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;
                case HEADER_TYPE_NORMAL:
                    holder.icon.setImageResource(header.iconRes);
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }

            return view;
        }

        public void resume() {
        }

        public void pause() {
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        // Override the fragment title for Wallpaper settings
        int titleRes = pref.getTitleRes();

        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, null, null, 0);
        return true;
    }

}
