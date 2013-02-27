package com.mikewadsten.test_umnclass;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import com.koushikdutta.widgets.BetterListFragment;
import com.koushikdutta.widgets.ListItem;

public class WidgetUtils {
    public static void buildGapDetails(BetterListFragment frag, int spaceID) {
        Gap gap = ClassroomContent.GAPMAP.get(spaceID);
        if (gap == null)
            gap = new Gap();
        buildGapDetails(frag, gap, null);
    }
    
    public static void buildGapDetails(BetterListFragment frag, Gap gap) {
        buildGapDetails(frag, gap, null);
    }
    
    public static void buildGapDetails(final BetterListFragment frag,
            Gap gap, SpaceInfo info) {
        try {
            // Remove sections already there.
            frag.removeSection(R.string.detail_section_location);
            frag.removeSection(R.string.detail_section_time);
            frag.removeSection(R.string.detail_section_features);
            frag.removeSection(R.string.detail_section_more);
            
            frag.addItem(R.string.detail_section_location,
                    new ListItem(frag, gap.getBuilding(), "Building",
                            R.drawable.ic_class_location));
            frag.addItem(R.string.detail_section_location,
                    new ListItem(frag, gap.getRoomNumber(), "Room",
                            R.drawable.ic_class_location));
            frag.addItem(R.string.detail_section_time,
                    new ListItem(frag, gap.getStartTime(), "Start time",
                            R.drawable.ic_class_time));
            frag.addItem(R.string.detail_section_time,
                    new ListItem(frag, gap.getEndTime(), "End time",
                            R.drawable.ic_class_time));
            
            if (info != null) {
                // Seat type
                frag.addItem(R.string.detail_section_features,
                        new ListItem(frag, info.getSeatType(), "Seat type",
                                R.drawable.ic_class_info));

                // Board type
                Drawable iconboard = frag.getResources()
                        .getDrawable(R.drawable.ic_class_board);
                
                if (info.hasChalk())
                    frag.addItem(R.string.detail_section_features, new ListItem(
                            frag, "Chalkboard", "Board type", iconboard));
                else if (info.hasMarkers())
                    frag.addItem(R.string.detail_section_features, new ListItem(
                            frag, "Markerboard", "Board type", iconboard));
                
                // Room capacity
                Drawable iconcapacity = frag.getResources()
                        .getDrawable(R.drawable.ic_class_capacity);
                frag.addItem(R.string.detail_section_features, new ListItem(
                        frag, Integer.toString(info.getCapacity()),
                        "Capacity", iconcapacity));
                
                // "More info" link
                final String url = info.getUrl();
                Drawable iconweb = frag.getResources()
                        .getDrawable(R.drawable.ic_class_web);
                frag.addItem(R.string.detail_section_more, new ListItem(
                        frag, "Classroom page", "Click to view", iconweb) {
                    @Override
                    public void onClick(View v) {
                        super.onClick(v);
                        final Intent intent =
                                new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse(url));
                        frag.startActivity(intent);
                    }
                });
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
