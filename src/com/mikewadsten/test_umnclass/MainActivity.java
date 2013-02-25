package com.mikewadsten.test_umnclass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.widgets.BetterListActivity;
import com.koushikdutta.widgets.BetterListFragment;
import com.koushikdutta.widgets.ListContentFragment;
import com.koushikdutta.widgets.ListItem;
import com.mikewadsten.test_umnclass.WebUtil.SearchURL;

public class MainActivity extends BetterListActivity {
    BetterListFragment mContent;
    private RefreshManager mRefresh;
    private MenuItem mSearchItem;

    private class RefreshManager {
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
            }

            isRefreshing = refreshing;

            if (isRefreshing)
                refreshIcon.setActionView(R.layout.action_bar_indeterminate_progress);
            else
                refreshIcon.setActionView(null);
        }
    }

    public MainActivity() {
        super(ListContentFragment.class);
    }

    public ListContentFragment getFragment() {
        return (ListContentFragment)super.getFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Get the search view and set it up correctly.
        mSearchItem = menu.findItem(R.id.search);
        SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView sv = (SearchView) mSearchItem.getActionView();
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        setupSearchHint(sv, getActionBar().getSelectedNavigationIndex());
        sv.setSubmitButtonEnabled(true);
        // show refreshing on load?
        MenuItem refresh = menu.findItem(R.id.refresh);
        if (refresh != null)
            mRefresh.setIcon(refresh);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        MenuItem refresh = menu.findItem(R.id.refresh);
        if (refresh != null)
            mRefresh.setIcon(refresh);
        return true;
    }

    private void setupSearchHint(SearchView sv, int position) {
        try {
            String campus = getResources()
                    .getStringArray(R.array.campus_list)[position];
            sv.setQueryHint(String.format("Search %s", campus));
        } catch (Exception e) {
            // Probably because getSelectedCampusName was called before
            // the activity was fully running, so the navigation index
            // is -1, which is a bad index into the name array...
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            startRefresh();
            return true;
        case R.id.settings:
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, 1);
            return true;
        case R.id.about:
            showAboutDialog(this);
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle icicle, final View view) {
        getFragment().setEmpty(R.string.empty_class_list);

        mRefresh = new RefreshManager();

        ActionBar bar = getActionBar();

        bar.setSubtitle(null);

        //        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter spin = ArrayAdapter.createFromResource(this,
                R.array.campus_list, R.layout.nav_item);

        OnNavigationListener navListener = new OnNavigationListener() {
            String[] campuses = getResources().getStringArray(R.array.campus_list);
            @Override
            public boolean onNavigationItemSelected(int pos, long itemId) {
                Log.d("CLASSES-nav",
                        String.format("Selected: %s", campuses[pos]));

                setupSearchHint((SearchView)mSearchItem.getActionView(), pos);
                startRefresh();
                // hopefully clears out contents
                if (mContent != null) {
                    backToList();
                }
                return true;
            }
        };
        bar.setListNavigationCallbacks(spin, navListener);

        //        view.findViewById(R.id.list_content_container).setPadding(8, 2, 8, 2);
        super.onCreate(icicle, view);
        
        // Search handling
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            
            // Collapse the search view
            if (mSearchItem != null)
                mSearchItem.collapseActionView();
            startRefresh(query);
        }
    }

    public void onBackPressed() {
        if (getFragment().onBackPressed()) {
            mContent = null; // not showing space info anymore
            return;
        }
        super.onBackPressed();
    }
    
    public void backToList() {
        try {
            // Show nothing on tablets
            getFragment().setContent(new Fragment(), true);
            mContent = null;
            // Go back to list on phones
            getFragment().onBackPressed();
        } catch (Exception e) {
            Log.e("MainActivity backToList", e.getMessage());
        }
    }

    public void startRefresh() {
        startRefresh(null);
    }
    
    /**
     * Start refresh, but with a search query too.
     * @param query
     */
    public void startRefresh(String query) {
        mRefresh.updateRefresh(true);
        String campus = getSelectedCampus();
        String defaultServer = getResources().getString(R.string.default_server);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SearchURL url = new SearchURL(
                prefs.getString("pref_search_url", defaultServer));
        
        url.query("campus", campus);
        if (query != null)
            url.query("search", query);
        new ClassSearch().setSearch(query).execute(url.toURL());
        if (mContent != null)
            backToList();
    }

    public String getSelectedCampus() {
        int index = getActionBar().getSelectedNavigationIndex();
        return getResources().getStringArray(R.array.campus_key_list)[index];
    }

    public String getSelectedCampusName() {
        int index = getActionBar().getSelectedNavigationIndex();
        return getResources().getStringArray(R.array.campus_list)[index];
    }

    void setContentGap(final int id) {
        //        Toast.makeText(MainActivity.this,
        //                String.format("Space: %d", id), Toast.LENGTH_SHORT).show();
        Bundle arguments = new Bundle();
        arguments.putInt(ClassroomDetailFragment.ARG_ITEM_ID, id);
        mContent = new ClassroomDetailFragment();
        mContent.setArguments(arguments);

        getFragment().setContent(mContent, false);
    }

    private void addGap(Gap g) {
        ClassroomContent.addItem(g);
        final int id = g.getGapId();
        String timespan = String.format("%s to %s",
                g.getStartTime(), g.getEndTime());
        addItem("Rooms", new ListItem(getFragment(), g.getFullName(), timespan){
            @Override
            public void onClick(View v) {
                super.onClick(v);

                setContentGap(id);
            }
        });
    }
    
    private void clearGaps() {
        try {
            getFragment().removeSection("Rooms");
        } catch (Exception e) {
            // wasn't there, alright.
        }
        try {
            getFragment().removeSection("Results");
        } catch (Exception e) {
            // wasn't there, alright.
        }
        ClassroomContent.clearItems();
    }

    private void searchResult(JSONArray arr, boolean success, String search) {
        if (success) {
            // We can't update data if we got nothing back.
            ArrayList<Gap> gaps = new ArrayList<Gap>();
            
            if (search != null) {
                String sub = String.format("Search: %s", search);
                getActionBar().setSubtitle(sub);
            }

            final int len = arr.length();
            if (len == 0 && search != null) {
                // No matches...
                setEmpty(R.string.empty_search_result);
                clearGaps();
                mRefresh.updateRefresh(false);
                return;
            }
            try {
                for (int i = 0; i < len; i++) {
                    try {
                        gaps.add(new Gap(arr.getJSONObject(i)));
                    } catch (Exception e) {
                        // Well that sucks
                        Log.e("Search result", e.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (gaps.size() > 0) {
                // Wipe out what's there
                clearGaps();
                for (Gap g : gaps) {
                    addGap(g);
//                    Log.d("Class gap", g.toString());
                }

                if (search == null) {
                    // only say "Updated at ..." if not search
                    ActionBar bar = getActionBar();
                    SimpleDateFormat fmt = new SimpleDateFormat(
                            "h:mm a", Locale.US);
                    bar.setSubtitle(
                            String.format("Updated at %s", 
                                            fmt.format(new Date())));
                }
            } else {
                if (search != null) {
                    // No matches.
                    setEmpty(R.string.empty_search_result);
                    clearGaps();
                }
                Log.i("Search result", "Got data, but nothing was parsed from it.");
            }
        }

        mRefresh.updateRefresh(false);
    }

    protected static class SearchResult {
        String result = "";
        boolean timeout = false;
        String search = null;
    }

    private class ClassSearch extends AsyncTask<String, Void, SearchResult> {
        private String queryString = null;
        
        public ClassSearch setSearch(String query) {
            queryString = query;
            return this;
        }
        
        @Override
        protected SearchResult doInBackground(String... urls) {
            SearchResult retval = new SearchResult();
            retval.search = queryString;
            String reply = "";
            try {
                String url = urls[0];
                Log.d("ClassSearch", String.format("Querying %s", url));
                DefaultHttpClient client = new DefaultHttpClient();
                final HttpParams params = client.getParams();
                // 3 seconds connection timeout
                HttpConnectionParams.setConnectionTimeout(params, 3000);
                // 10 seconds data timeout
                HttpConnectionParams.setSoTimeout(params, 10000);
                HttpGet get = new HttpGet(url);
                // tell the server this is an XHR. I'm working on modifying the Flask app
                // to use 'jsonify', which only produces condensed JSON output if the request
                // specifies it is an XHR. condensed output is much much better and quicker to
                // pull down than non-condensed ( json.dumps(_, indent=2) ) so we want to make
                // sure that what we get (no pun intended) is as small as we can get it
                get.addHeader("X-Requested-With", "XMLHttpRequest");
                HttpResponse ex = client.execute(get);
                InputStream is = ex.getEntity().getContent();

                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                String s = "";
                while ((s = buf.readLine()) != null) {
                    reply += s;
                }

                Log.d("Classes Search", String.format("Read %d",
                        reply.length()));
                retval.result = reply;
            } catch (SocketTimeoutException soe) {
                Log.e("Classes Search", "Socket timed out");
                retval.result = "Connection timed out while contacting server.";
                retval.timeout = true;
            } catch (Exception e) {
                e.printStackTrace();
                retval.result = String.format("Error: %s", e.getMessage());
            }

            return retval;
        }

        @Override
        protected void onPostExecute(SearchResult result) {
            String error = "Error: Got bad response from server.";

            if (result.timeout) {
                error = result.result; // nice way of putting it
            }
            else { // parse the result or something
                try {
                    JSONObject obj = new JSONObject(result.result);
                    if (obj.has("error")) {
                        error = String.format("Server error: %s",
                                obj.getString("error"));
                        Log.e("Class search", error);
                    }
                    else if (obj.has("rooms")) {
                        searchResult(obj.getJSONArray("rooms"), true, result.search);
                        return;
                    }
                } catch (JSONException e) {
                    error = e.getMessage();
                    Log.w("CLA.oPE", error);
                    e.printStackTrace();
                    error = String.format("Error: %s", error);
                }
            }

            // Show a Toast to indicate what's up
            Toast.makeText(getApplicationContext(), error,
                    Toast.LENGTH_SHORT).show();
            searchResult(null, false, result.search);
        }
    }
    
    public static void showAboutDialog(BetterListActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null)
            ft.remove(prev);
        ft.addToBackStack(null);
        
        new AboutDialog().show(ft, "dialog_about");
//        ft.commit();
    }

    // AboutDialog code "inspired" (read - stolen) from Roman Nurik's
    // DashClock code.
    public static class AboutDialog extends DialogFragment {
        private static final String VERSION_UNAVAILABLE = "N/A";

        public AboutDialog() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get app version
            PackageManager pm = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            String versionName;
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                versionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = VERSION_UNAVAILABLE;
            }

            // Build the about body view
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View rootView = layoutInflater.inflate(R.layout.dialog_about, null);
            TextView nameAndVersion = (TextView) rootView
                    .findViewById(R.id.app_name_and_version);
            nameAndVersion.setText(
                    Html.fromHtml(
                            String.format("<b>Classroom Finder</b>&nbsp;<font color=\"#888888\">v%s</font>", versionName)));
            ((TextView)rootView.findViewById(R.id.about_body))
            .setText(Html.fromHtml(getString(R.string.about_body)));
            ((TextView)rootView.findViewById(R.id.about_body))
            .setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity())
            .setView(rootView)
            .setPositiveButton(R.string.close,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
            })
            .create();
        }
    }
}
