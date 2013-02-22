package com.mikewadsten.test_umnclass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            mRefresh.updateRefresh(true);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            new Search().execute(prefs.getString("pref_server_url", "http://mikewadsten.com/test.json"));
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
            ((ClassroomListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.classroom_list))
                    .setActivateOnItemClick(true);
        }

        mHandler = new Handler();

        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        mRefresh = new RefreshManager();

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.campus_list,
                android.R.layout.simple_spinner_dropdown_item);
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
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ClassroomDetailFragment.ARG_ITEM_ID, id);
            ClassroomDetailFragment fragment = new ClassroomDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
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
            ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
            try {
                int len = arr.length();
                for (int i = 0; i < len; i++) {
                    try {
                        objs.add(arr.getJSONObject(i));
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    
            ClassroomContent.ITEMS.clear();
            ClassroomContent.ITEM_MAP.clear();
    
            for (int i = 0; i < objs.size(); i++) {
                JSONObject o = objs.get(i);
                String content = (String) o.keys().next();
                ClassroomContent.addItem(new ClassroomContent.Classroom(Integer.toString(i), content));
            }
            
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ClassroomListFragment f = (ClassroomListFragment) getSupportFragmentManager().findFragmentById(R.id.classroom_list);
                        if (f == null) {
                            Log.e("Classes.update", "list fragment null");
                            return;
                        }
                        @SuppressWarnings("rawtypes") // Silly typecasting...
                        ArrayAdapter a = (ArrayAdapter)f.getListAdapter();
                        if (a == null) {
                            Log.e("Classes.update", "list adapter null");
                            return;
                        }
                        a.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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
                DefaultHttpClient client = new DefaultHttpClient();
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

            Log.d("Search result", String.format("%s", reply));

            return reply;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray obj = new JSONArray(result);
//                Toast.makeText(getApplicationContext(), obj.toString(2), Toast.LENGTH_SHORT).show();
                searchResult(obj, true);
            } catch (JSONException e) {
                Log.w("CLA.oPE", result);
                e.printStackTrace();
                String err = "Error: Got bad response from server.";
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                searchResult(new JSONArray(), false);
            }
        }
    }
}
