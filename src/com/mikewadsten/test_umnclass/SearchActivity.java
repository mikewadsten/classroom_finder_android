package com.mikewadsten.test_umnclass;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.koushikdutta.widgets.BetterListActivity;
import com.koushikdutta.widgets.ListContentFragment;

public class SearchActivity extends BetterListActivity {
    //============================================
    // Needed for correct UI on tablets and phones
    
    public SearchActivity() {
        super(ListContentFragment.class);
    }

    public ListContentFragment getFragment() {
        return (ListContentFragment)super.getFragment();
    }
    //===========================================
    
    @Override
    public void onCreate(Bundle icicle, View v) {
        getFragment().setEmpty(R.string.search_not_imp);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(icicle, v);
        
        // Set action bar subtitle to show query
        Intent intent = getIntent();
        String query = intent.getStringExtra(SearchManager.QUERY);
        String format = getResources().getString(R.string.search_subtitle);
        getActionBar().setSubtitle(String.format(format, query));
//        addItem("Results", new ListItem(getFragment(), R.string.app_name, 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return false;
        }
    }

}
