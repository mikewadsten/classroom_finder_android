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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.widgets.BetterListFragment;
import com.koushikdutta.widgets.ListItem;
import com.mikewadsten.test_umnclass.WebUtil.SearchURL;


/**
 * Fragment to present classroom opening details.
 */
public class ClassroomDetailFragment extends BetterListFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	
	private Gap gap;

	public ClassroomDetailFragment setGap(Gap gap) {
	    this.gap = gap;
	    return this;
	}
	
    @Override
    protected void onCreate(Bundle icicle, View view) {
        super.onCreate(icicle);
        
        SpaceInfo space = null;
        if (gap == null) {
            gap = new Gap();
            gap.setBuilding("Placeholder...");
            gap.setRoomNumber("Unknown");
            gap.setStartTime("midnight");
            gap.setEndTime("11:59pm");
            gap.setGapLength(444);
            gap.setSpaceId(-1);
        } else {
            // Fetch the space info if there is one
            space = ClassroomContent.SPACEMAP.get(gap.getSpaceId());
        }
        
        // Make UI
        WidgetUtils.buildGapDetails(this, gap, space);
        
        if (space == null) {
            // Need to download space info, whoop whoop
            addItem(R.string.detail_section_features, new ListItem(
                    this, "Loading...", null, 0));
            
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SearchURL url = new SearchURL(
                    prefs.getString("pref_info_url",
                            getString(R.string.default_server_space)));
            url.query("spaceID", Integer.toString(gap.getSpaceId()));
            new SpaceSearch().execute(url.toURL());
        }
    }
    
    private void searchResult(JSONObject info, boolean success) {
        if (success) {
            try {
                SpaceInfo spaceinfo = new SpaceInfo(info);
                WidgetUtils.buildGapDetails(this, gap, spaceinfo);
                // save off the space info
                ClassroomContent.SPACEMAP.put(spaceinfo.getSpaceId(), spaceinfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                WidgetUtils.buildGapDetails(this, gap, null);
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
                Log.d("Gap Search", String.format("Querying %s", url));
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

                Log.d("Gap Search", String.format("%s", reply));
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
            try {
                Toast.makeText(getActivity(),
                        error, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // fragment gone probably...
            }
        }
    }
}
