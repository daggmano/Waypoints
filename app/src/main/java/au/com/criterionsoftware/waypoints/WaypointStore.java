package au.com.criterionsoftware.waypoints;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by darrenoster on 12/12/16.
 */

interface OnWaypointSummaryChanged {
	void onWaypointSummaryChange(int waypointCount, int distance);
}

class WaypointStore {

	private static final String LOG_TAG = WaypointStore.class.getSimpleName();

	static final String WAYPOINT_KEY = "waypoints";

	private ArrayList<LatLng> waypoints;

	private OnWaypointSummaryChanged delegate;

	WaypointStore(OnWaypointSummaryChanged delegate) {
		this.delegate = delegate;

		waypoints = new ArrayList<>();
		updateSummary();
	}

	void addWaypoint(LatLng latLng) {
		waypoints.add(latLng);
		updateSummary();
	}

	void insertWaypoint(LatLng latLng, int index) {
		waypoints.add(index + 1, latLng);
		updateSummary();
	}

	void saveState(Bundle bundle) {
		bundle.putParcelableArrayList(WAYPOINT_KEY, waypoints);
	}

	void restoreState(Bundle bundle) {
		if (bundle != null && bundle.containsKey(WAYPOINT_KEY)) {
			waypoints = bundle.getParcelableArrayList(WAYPOINT_KEY);
			updateSummary();
		}
	}

	void removeWaypointAt(int index) {
		waypoints.remove(index);
		updateSummary();
	}

	LatLng[] getWaypointsArray() {
		LatLng[] result = new LatLng[waypoints.size()];
		return waypoints.toArray(result);
	}

	private void updateSummary() {
		if (waypoints.size() < 2) {
			delegate.onWaypointSummaryChange(waypoints.size(), 0);
		}

		float totalDistance = 0;
		for (int i = 1; i < waypoints.size(); i++) {
			totalDistance += SphericalUtil.computeDistanceBetween(waypoints.get(i - 1), waypoints.get(i));
		}

		delegate.onWaypointSummaryChange(waypoints.size(), (int) totalDistance);
	}
}
