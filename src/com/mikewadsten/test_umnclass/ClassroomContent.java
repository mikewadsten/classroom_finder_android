package com.mikewadsten.test_umnclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static List<Classroom> ITEMS = new ArrayList<Classroom>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, Classroom> ITEM_MAP = new HashMap<String, Classroom>();

	static {
		// Add 3 sample items.
		addItem(new Classroom("1", "Item 1"));
		addItem(new Classroom("2", "Item 2"));
		addItem(new Classroom("3", "Item 3"));
	}

	public static void addItem(Classroom item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class Classroom {
		public String id;
		public String content;

		public Classroom(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
