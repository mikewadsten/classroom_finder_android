package com.mikewadsten.test_umnclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.util.SparseArray;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ClassroomContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<Gap> GAPS = new ArrayList<Gap>();
	public static SparseArray<Gap> GAPMAP = new SparseArray<Gap>();
	public static SparseArray<SpaceInfo> SPACEMAP = new SparseArray<SpaceInfo>();

	public static void addItem(Gap item) {
		GAPS.add(item);
		GAPMAP.put(item.getGapId(), item);
	}
	
	public static void addSpace(SpaceInfo space) {
	    SPACEMAP.put(space.getSpaceId(), space);
	}
	
	public static void addAll(Collection<Gap> gaps) {
	    for (Gap g : gaps) addItem(g);
	}
	
	public static void clearItems() {
	    GAPS.clear();
	    GAPMAP.clear();
	    SPACEMAP.clear();
	}
}
