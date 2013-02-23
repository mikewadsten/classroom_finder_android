package com.mikewadsten.test_umnclass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

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
public class ClassroomListActivity extends Activity implements
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            // Shouldn't need the default since the preference does exist. But whatever.
            String defaultServer = getResources().getString(R.string.default_server);
            new Search().execute(prefs.getString("pref_server_url", defaultServer));
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
            ((ClassroomListFragment) getFragmentManager()
                    .findFragmentById(R.id.classroom_list))
                    .setActivateOnItemClick(true);
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
            Bundle arguments = new Bundle();
            arguments.putInt(ClassroomDetailFragment.ARG_ITEM_ID, id);
            ClassroomDetailFragment fragment = new ClassroomDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
            .replace(R.id.classroom_detail_container, fragment)
            .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this,
                    ClassroomDetailActivity.class);
            detailIntent.putExtra(ClassroomDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
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
                FragmentManager fm = getFragmentManager();
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

    private class Search extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String reply = "";
            try {
                String url = urls[0];
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
            } catch (Exception e) {
                e.printStackTrace();
                return String.format("Error: %s", e.toString());
            }

            Log.d("Classes Search", String.format("Read %d", reply.length()));
            return reply;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray obj = new JSONArray(result);
                searchResult(obj, true);
            } catch (JSONException e) {
                String error = e.getMessage();
                try {
                    // Errors should be in format {"error": ...}
                    JSONObject err_obj = new JSONObject(result);
                    error = String.format("Server error: %s",
                            err_obj.getString("error"));
                    Log.e("Class search", error);
                } catch (Exception e2) {
                    Log.w("CLA.oPE", result);
                    e2.printStackTrace();
                    error = "Error: Got bad response from server.";
                }
                Toast.makeText(getApplicationContext(), error,
                        Toast.LENGTH_LONG).show();
                searchResult(null, false);
            }
        }
    }
}
