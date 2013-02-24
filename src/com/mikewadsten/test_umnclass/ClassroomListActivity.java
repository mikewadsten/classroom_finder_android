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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.koushikdutta.widgets.ActivityBaseFragment;

/**
 * An activity representing a list of Classrooms. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ClassroomDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ClassroomListFragment} and the item details (if present) is a
 * {@link ClassroomDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ClassroomListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ClassroomListActivity extends FragmentActivity implements
ClassroomListFragment.Callbacks {
    
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem refresh = menu.findItem(R.id.refresh);
        if (refresh != null) {
            mRefresh.setIcon(refresh);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.main_menu, menu);
        
        // Get the search one and set it up?
        SearchView sv = (SearchView) menu.findItem(R.id.search).getActionView();
        sv.setQueryHint("Search all rooms");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            mRefresh.updateRefresh(true);
            new Search().execute("?campus=stpaul");
            return true;
        case R.id.settings:
            Intent settingsIntent = new Intent(this,
                    SettingsActivity.class);
            startActivityForResult(settingsIntent, 1);
            return true;
        }
        return false;
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    @SuppressWarnings("unused")
	private Handler mHandler;
    private RefreshManager mRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_list);

        if (findViewById(R.id.classroom_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ClassroomListFragment frag =
                    (ClassroomListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.classroom_list);

            frag.setActivateOnItemClick(true);
            
            setDetailWeight(2);
        }

        mHandler = new Handler();

        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        bar.setSubtitle(null);
        mRefresh = new RefreshManager();

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.campus_list,
                R.layout.nav_item);
                //android.R.layout.simple_dropdown_item_1line);
        OnNavigationListener navListener = new OnNavigationListener() {
            String[] campuses = getResources().getStringArray(R.array.campus_list);
            @Override
            public boolean onNavigationItemSelected(int itemPosition,
                    long itemId) {
                mRefresh.updateRefresh(true);
                new Search().execute("?campus=east");
                Log.d("CLASSES-nav", String.format("Selected: %s", campuses[itemPosition]));
                return true;
            }
        };
        
        bar.setListNavigationCallbacks(mSpinnerAdapter, navListener);
    }

    /**
     * Callback method from {@link ClassroomListFragment.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            ActivityBaseFragment fragment = new ActivityBaseFragment();
            WidgetUtils.buildGapDetails(fragment, id);
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.classroom_detail_container, fragment)
            .commit();
            setDetailWeight(2);
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this,
                    ClassroomDetailActivity.class);
            detailIntent.putExtra(ClassroomDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
    
    private void setDetailWeight(float weight) {
        LayoutParams p = new LinearLayout.LayoutParams(0,
                LayoutParams.MATCH_PARENT, weight);
        findViewById(R.id.classroom_detail_container).setLayoutParams(p);
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
                ClassroomContent.clearItems();
                ClassroomContent.addAll(gaps);
                
                // Notify the adapter that the data has changed
                FragmentManager fm = getSupportFragmentManager();
                ClassroomListFragment frag =
                        (ClassroomListFragment)fm.findFragmentById(R.id.classroom_list);
                ((BaseAdapter) frag.getListAdapter()).notifyDataSetChanged();
                
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

    private class Search extends AsyncTask<String, Void, SearchResult> {

        @Override
        protected SearchResult doInBackground(String... urls) {
            SearchResult retval = new SearchResult();
            String reply = "";
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ClassroomListActivity.this);
                // Shouldn't need the default since the preference does exist. But whatever.
                String defaultServer = getResources().getString(R.string.default_server);
                String baseUrl = prefs.getString("pref_server_url", defaultServer);
                String url = baseUrl.concat(urls[0]);
                Log.d("Classes Search", String.format("Querying %s", url));
                DefaultHttpClient client = new DefaultHttpClient();
                final HttpParams params = client.getParams();
                // 3 seconds connection timeout
                HttpConnectionParams.setConnectionTimeout(params, 3000);
                // 10 seconds data timeout
                HttpConnectionParams.setSoTimeout(params, 10000);
                HttpGet get = new HttpGet(url);
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
