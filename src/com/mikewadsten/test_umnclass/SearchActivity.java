package com.mikewadsten.test_umnclass;

import android.os.Bundle;
import android.view.View;

import com.koushikdutta.widgets.ActivityBase;
import com.koushikdutta.widgets.ListContentFragment;

public class SearchActivity extends ActivityBase {
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
        getFragment().setEmpty(R.string.empty_search_result);
        super.onCreate(icicle, v);
//        addItem("Results", new ListItem(getFragment(), R.string.app_name, 0));
    }

}
