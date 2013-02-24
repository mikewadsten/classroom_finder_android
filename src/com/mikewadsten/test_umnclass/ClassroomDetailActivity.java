package com.mikewadsten.test_umnclass;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.koushikdutta.widgets.ActivityBase;

/**
 * An activity representing a single Classroom detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a
 * {@link ClassroomListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ClassroomDetailFragment}.
 */
public class ClassroomDetailActivity extends ActivityBase {
	@Override
	public void onCreate(Bundle savedInstanceState, View view) {
		super.onCreate(savedInstanceState, view);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			int itemId = getIntent().getIntExtra(
			        ClassroomDetailFragment.ARG_ITEM_ID, -1);
			WidgetUtils.buildGapDetails(getFragment(), itemId);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
