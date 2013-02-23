package com.mikewadsten.test_umnclass;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GapAdapter extends ArrayAdapter<Gap> {
    private SparseArray<Gap> items;
    private Context context;
    private int layoutId;
    
    protected static class GapView {
        protected TextView name;
        protected TextView info;
    }

    public GapAdapter(Context context, int layoutId, SparseArray<Gap> objects) {
        super(context, layoutId);
        this.items = objects;
        this.context = context;
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Gap getItem(int position) {
        return ClassroomContent.GAPS.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // http://android.vexedlogic.com/2011/04/02/android-lists-listactivity-and-listview-ii-%E2%80%93-custom-adapter-and-list-item-view/
        View v = convertView;
        GapView gap = null;
        if (v == null) {
            LayoutInflater i = ((Activity)context).getLayoutInflater();
            v = i.inflate(layoutId, parent, false);
            
            gap = new GapView();
//            gap.name = (TextView)v.findViewById(R.id.roomname);
//            gap.info = (TextView)v.findViewById(R.id.gapinfo);
            gap.name = (TextView)v.findViewById(android.R.id.text1);
            gap.info = (TextView)v.findViewById(android.R.id.text2);
            
            v.setTag(gap);
        }
        else {
            gap = (GapView)v.getTag();
        }
        
        Gap item = ClassroomContent.GAPS.get(position);
        gap.name.setText(item.getRoomName());
        gap.info.setText(
                String.format("%s to %s (%d min)", item.getStartTime(),
                        item.getEndTime(), item.getGapLength()));
        v.setId(position);
        
        return v;
    }

}
