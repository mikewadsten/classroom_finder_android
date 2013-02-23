package com.mikewadsten.test_umnclass;

import com.koushikdutta.widgets.ActivityBase;
import com.koushikdutta.widgets.ListContentFragment;

public class MainActivity extends ActivityBase {
    public MainActivity() {
        super(ListContentFragment.class);
    }
    
    public ListContentFragment getFragment() {
        return (ListContentFragment)super.getFragment();
    }
}
