/*
 * Copyright (C) 2010 Paul Watts (paulcwatts@gmail.com)
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
package com.joulespersecond.seattlebusbot;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;


abstract class MyTabActivityBase extends SherlockFragmentActivity {
    public static final String EXTRA_SHORTCUTMODE = ".ShortcutMode";
    public static final String EXTRA_SEARCHCENTER = ".SearchCenter"; //int[]

    public static final String RESULT_ROUTE_ID = ".RouteId";

    protected boolean mShortcutMode;
    protected GeoPoint mSearchCenter;
    private String mDefaultTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        UIHelp.setupActionBar(this);
        setSupportProgressBarIndeterminateVisibility(false);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        mShortcutMode = Intent.ACTION_CREATE_SHORTCUT.equals(action);
        if (!mShortcutMode) {
            setTitle(R.string.app_name);
        }

        mSearchCenter = getSearchCenter(intent);

        // Determine what tab we're supposed to show by default
        if (savedInstanceState != null) {
            mDefaultTab = savedInstanceState.getString("tab");
        }
        final Uri data = intent.getData();
        if (data != null && mDefaultTab == null) {
            mDefaultTab = getDefaultTabFromUri(data);
        }
    }

    @Override
    public void onDestroy() {
        // If there was a tab in the intent, don't save it
        if (mDefaultTab == null) {
            final ActionBar bar = getSupportActionBar();
            final ActionBar.Tab tab = bar.getSelectedTab();
            PreferenceHelp.saveString(getLastTabPref(), (String)tab.getTag());
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavHelp.goHome(this);
            return true;
        }
        return false;
    }

    protected void restoreDefaultTab() {
        final String def;
        if (mDefaultTab != null) {
            def = mDefaultTab;
        } else {
            SharedPreferences settings = Application.getPrefs();
            def = settings.getString(getLastTabPref(), null);
        }
        if (def != null) {
            // Find this tab...
            final ActionBar bar = getSupportActionBar();
            for (int i=0; i < bar.getTabCount(); ++i) {
                ActionBar.Tab tab = bar.getTabAt(i);
                if (def.equals(tab.getTag())) {
                    tab.select();
                }
            }
        }
    }

    //
    // Accessors for tabs
    //
    boolean isShortcutMode() {
        return mShortcutMode;
    }

    GeoPoint getSearchCenter() {
        return mSearchCenter;
    }

    //
    // Helpers for constructing/parsing the default tab URL.
    //
    private static final String TAB_SCHEME = "tab";

    public static final Uri getDefaultTabUri(String tab) {
        return Uri.fromParts(TAB_SCHEME, tab, null);
    }
    public static String getDefaultTabFromUri(Uri uri) {
        if (TAB_SCHEME.equals(uri.getScheme())) {
            return uri.getSchemeSpecificPart();
        }
        return null;
    }

    protected abstract String getLastTabPref();

    //
    // Helper for getting the search center from the intent
    //
    public static final Intent putSearchCenter(Intent intent, GeoPoint pt) {
        if (pt != null) {
            int[] p = { pt.getLatitudeE6(), pt.getLongitudeE6() };
            intent.putExtra(EXTRA_SEARCHCENTER, p);
        }
        return intent;
    }

    private static final GeoPoint getSearchCenter(Intent intent) {
        int[] p = intent.getIntArrayExtra(EXTRA_SEARCHCENTER);
        if (p != null && p.length == 2) {
            return new GeoPoint(p[0], p[1]);
        }
        return null;
    }
}
