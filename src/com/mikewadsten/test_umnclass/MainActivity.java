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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.koushikdutta.widgets.ActivityBase;
import com.koushikdutta.widgets.ActivityBaseFragment;
import com.koushikdutta.widgets.ListContentFragment;
import com.koushikdutta.widgets.ListItem;
import com.mikewadsten.test_umnclass.WebUtil.SearchURL;

public class MainActivity extends ActivityBase {
    ActivityBaseFragment mContent;
    private RefreshManager mRefresh;

    private class RefreshManager {
        private MenuItem refreshIcon;
        private boolean isRefreshing;

        public RefreshManager() {
        }

        public void setIcon(MenuItem icon) {
            refreshIcon = icon;
        }

        public void updateRefresh(boolean refreshing) {
            if (refreshIcon == null)
                return;

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
        SearchView sv = (SearchView) menu.findItem(R.id.search).getActionView();
        sv.setQueryHint("Search all rooms");
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
        }
        return false;
    }

    @Override
    public void onCreate(Bundle icicle, View view) {
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
            public boolean onNavigationItemSelected(int itemPosition,
                    long itemId) {
                //                mRefresh.updateRefresh(true);
                //                new Search().execute("?campus=east");
                Log.d("CLASSES-nav", String.format("Selected: %s", campuses[itemPosition]));
                startRefresh();
                return true;
            }
        };
        bar.setListNavigationCallbacks(spin, navListener);

        //        view.findViewById(R.id.list_content_container).setPadding(8, 2, 8, 2);
        super.onCreate(icicle, view);
        view.findViewById(R.id.listview).setPadding(0, 0, 0, 0);
    }

    public void onBackPressed() {
        if (getFragment().onBackPressed())
            return;
        super.onBackPressed();
    }
    
    public void startRefresh() {
        mRefresh.updateRefresh(true);
        String campus = getSelectedCampus();
        String defaultServer = getResources().getString(R.string.default_server);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SearchURL url = new SearchURL(
                prefs.getString("pref_search_url", defaultServer));
        url.query("campus", campus);
        new ClassSearch().execute(url.toString());
    }
    
    public String getSelectedCampus() {
        int index = getActionBar().getSelectedNavigationIndex();
        return getResources().getStringArray(R.array.campus_key_list)[index];
    }

    void setContentGap(final int id) {
//        Toast.makeText(MainActivity.this,
//                String.format("Space: %d", id), Toast.LENGTH_SHORT).show();
        Bundle arguments = new Bundle();
        arguments.putInt(ClassroomDetailFragment.ARG_ITEM_ID, id);
        mContent = new ClassroomDetailFragment();
        mContent.setArguments(arguments);

        getFragment().setContent(mContent);
    }

    private void addGap(Gap g) {
        ClassroomContent.addItem(g);
        final int id = g.getGapId();
        String timespan = String.format("%s to %s (%d min)", g.getStartTime(),
                g.getEndTime(), g.getGapLength());
        addItem("Rooms", new ListItem(getFragment(), g.getFullName(), timespan){
            @Override
            public void onClick(View v) {
                super.onClick(v);

                setContentGap(id);
            }
        });
    }
    
    private void searchResult(JSONArray arr, boolean success) {
        if (success) {
            // We can't update data if we got nothing back.
            ArrayList<Gap> gaps = new ArrayList<Gap>();
            
            final int len = arr.length();
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
                getFragment().removeSection("Rooms");
                ClassroomContent.clearItems();
                for (Gap g : gaps) {
                    addGap(g);
                    Log.d("Class gap", g.toString());
                }
                
                ActionBar bar = getActionBar();
                SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.US);
                bar.setSubtitle(String.format("Updated at %s", fmt.format(new Date())));
            } else {
                Log.i("Search result", "Got data, but nothing was parsed from it.");
            }
        }
        
        mRefresh.updateRefresh(false);
    }
    
    protected static class SearchResult {
        String result = "";
        boolean timeout = false;
    }

    private class ClassSearch extends AsyncTask<String, Void, SearchResult> {

        @Override
        protected SearchResult doInBackground(String... urls) {
            SearchResult retval = new SearchResult();
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
                get.addHeader('X-Requested-With', 'XMLHttpRequest');
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
            String error;
            
            if (result.timeout) {
                error = result.result; // nice way of putting it
            }
            else { // parse the result or something
                try {
                    JSONArray obj = new JSONArray(result.result);
                    searchResult(obj, true);
                    return;
                } catch (JSONException e) {
                    error = e.getMessage();
                    try {
                        // Errors should be in format {"error": ...}
                        JSONObject err_obj = new JSONObject(result.result);
                        error = String.format("Server error: %s",
                                err_obj.getString("error"));
                        Log.e("Class search", error);
                    } catch (Exception e2) {
                        Log.w("CLA.oPE", result.result);
                        e2.printStackTrace();
                        error = "Error: Got bad response from server.";
                    }
                }
            }
            
            // Show a Toast to indicate what's up
            Toast.makeText(getApplicationContext(), error,
                    Toast.LENGTH_SHORT).show();
            searchResult(null, false);
        }
    }
}
