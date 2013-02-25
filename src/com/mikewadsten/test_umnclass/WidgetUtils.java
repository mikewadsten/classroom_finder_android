package com.mikewadsten.test_umnclass;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
            frag.addItem("Location",
                    new ListItem(frag, gap.getBuilding(), "Building",
                            R.drawable.ic_class_location));
            frag.addItem("Location",
                    new ListItem(frag, gap.getRoomNumber(), "Room",
                            R.drawable.ic_class_location));
            frag.addItem("Availability",
                    new ListItem(frag, gap.getStartTime(), "Start time",
                            R.drawable.ic_class_time));
            frag.addItem("Availability",
                    new ListItem(frag, gap.getEndTime(), "End time",
                            R.drawable.ic_class_time));
            
            if (info != null) {
                // Load space info into page
                frag.addItem("Room Features",
                        new ListItem(frag, info.getSeatType(), "Seat type",
                                R.drawable.ic_class_info));
                
                Drawable iconboard = frag.getResources()
                        .getDrawable(R.drawable.ic_class_board);
                if (info.hasChalk())
                    frag.addItem("Room Features", new ListItem(frag,
                            "Chalkboard", "Board type", iconboard));
                else if (info.hasMarkers())
                    frag.addItem("Room Features", new ListItem(frag,
                            "Markerboard", "Board type", iconboard));
                
                Drawable iconcapacity = frag.getResources()
                        .getDrawable(R.drawable.ic_class_capacity);
                frag.addItem("Room Features", new ListItem(
                        frag, Integer.toString(info.getCapacity()),
                        "Capacity", iconcapacity));
                
                final String url = info.getUrl();
                Drawable iconweb = frag.getResources()
                        .getDrawable(R.drawable.ic_class_web);
                frag.addItem("More info",
                        new ListItem(frag,
                                "Classroom page", "Click to view", iconweb) {
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
