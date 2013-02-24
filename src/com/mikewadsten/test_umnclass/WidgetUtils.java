package com.mikewadsten.test_umnclass;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;

import com.koushikdutta.widgets.ActivityBaseFragment;
import com.koushikdutta.widgets.ListItem;

public class WidgetUtils {
    public static void buildGapDetails(ActivityBaseFragment frag, int spaceID) {
        Gap gap = ClassroomContent.GAPMAP.get(spaceID);
        if (gap == null)
            gap = new Gap();
        buildGapDetails(frag, gap, null);
    }
    
    public static void buildGapDetails(ActivityBaseFragment frag, Gap gap) {
        buildGapDetails(frag, gap, null);
    }
    
    public static void buildGapDetails(final ActivityBaseFragment frag,
            Gap gap, SpaceInfo info) {
        try {
            frag.removeSection("Location");
            frag.removeSection("Availability");
            frag.removeSection("Room Features");
            frag.addItem("Location", new ListItem(frag, gap.getBuilding(), "Building"));
            frag.addItem("Location", new ListItem(frag, gap.getRoomNumber(), "Room"));
            frag.addItem("Availability", new ListItem(frag, gap.getStartTime(), "Start time"));
            frag.addItem("Availability", new ListItem(frag, gap.getEndTime(), "End time"));
            
            if (info != null) {
                // Load space info into page
                frag.addItem("Room Features", new ListItem(frag, info.getSeatType(), "Seat type"));
                if (info.hasChalk())
                    frag.addItem("Room Features", new ListItem(frag, "Chalkboard", "Board type"));
                else if (info.hasMarkers())
                    frag.addItem("Room Features", new ListItem(frag, "Markerboard", "Board type"));
                
                frag.addItem("Room Features", new ListItem(frag, Integer.toString(info.getCapacity()), "Capacity"));
                
                final String url = info.getUrl();
                frag.addItem("More info",
                        new ListItem(frag, "Classroom page", "Click to view",
                                null) {
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
