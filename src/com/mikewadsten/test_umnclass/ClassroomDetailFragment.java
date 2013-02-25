package com.mikewadsten.test_umnclass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.koushikdutta.widgets.BetterListFragment;


/**
 * Fragment to present classroom opening details.
 */
public class ClassroomDetailFragment extends BetterListFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	
	private Gap mGap;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ClassroomDetailFragment() {
	}
	
	@Override
	public void onConfigurationChanged(Configuration config) {
	    super.onConfigurationChanged(config);
	    // Reload information!
	    ((MainActivity)getActivity()).setContentGap(mGap.getGapId());
	}
//
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        getActivity().getActionBar()
//            .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case android.R.id.home:
//            getActivity().onBackPressed();
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void onCreate(Bundle icicle, View view) {
        super.onCreate(icicle);
        
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM_ID))
            mGap = ClassroomContent.GAPMAP.get(getArguments().getInt(ARG_ITEM_ID));
        
        if (mGap == null) {
            mGap = new Gap();
            mGap.setBuilding("Placeholder...");
            mGap.setRoomNumber("Unknown");
            mGap.setStartTime("midnight");
            mGap.setEndTime("11:59pm");
            mGap.setGapLength(444);
            mGap.setSpaceId(1000000);
        }
        // Make UI
        WidgetUtils.buildGapDetails(this, mGap);
        
        new SpaceSearch().execute(
                String.format("http://mikewadsten.com/classroom_finder/space.cgi?spaceID=%d", mGap.getSpaceId()));
    }
    
    private void searchResult(JSONObject info, boolean success) {
        if (success) {
            try {
                SpaceInfo spaceinfo = new SpaceInfo(info);
                WidgetUtils.buildGapDetails(this, mGap, spaceinfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected static class SearchResult {
        String result = "";
        boolean timeout = false;
    }
    
    private class SpaceSearch extends AsyncTask<String, Void, SearchResult> {

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
                Log.e("Classes Search", e.getMessage());
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
                    JSONObject obj = new JSONObject(result.result);
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
            searchResult(null, false);
        }
    }
}
