package com.mikewadsten.test_umnclass;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment representing a single Classroom detail screen. This fragment is
 * either contained in a {@link ClassroomListActivity} in two-pane mode (on
 * tablets) or a {@link ClassroomDetailActivity} on handsets.
 */
public class ClassroomDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Gap mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ClassroomDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = ClassroomContent.GAPMAP.get(getArguments().getInt(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_classroom_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.classroom_detail))
					.setText(mItem.getRoomName());
		}
		
		ActionBar ab = getActivity().getActionBar();
		ab.setTitle(mItem.getRoomName());
		
		String subtitle = String.format("Open %s until %s",
		        mItem.getStartTime(), mItem.getEndTime());
		ab.setSubtitle(subtitle);
		
		return rootView;
	}
}
