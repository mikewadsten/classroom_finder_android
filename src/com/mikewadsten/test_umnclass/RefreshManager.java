package com.mikewadsten.test_umnclass;

import android.util.Log;
import android.view.MenuItem;

/**
 * Essentially a utility class to easily handle setting the action view on
 * the "refresh" menu item on and off depending on what's happening
 * @author Mike
 *
 */
public class RefreshManager {
    private MenuItem refreshIcon;
    private boolean isRefreshing;

    public RefreshManager() {
    }

    public void setIcon(MenuItem icon) {
        refreshIcon = icon;
    }

    public void updateRefresh(boolean refreshing) {
        if (refreshIcon == null) {
            Log.d("updateRefresh", "icon is null");
            return;
        }

        isRefreshing = refreshing;

        if (isRefreshing)
            refreshIcon.setActionView(R.layout.action_bar_indeterminate_progress);
        else
            refreshIcon.setActionView(null);
    }
}