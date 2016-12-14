package au.com.criterionsoftware.waypoints;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by darrenoster on 12/12/16.
 */

public class WaypointStore {

	private static final String LOG_TAG = WaypointStore.class.getSimpleName();

	static final String WAYPOINT_KEY = "waypoints";

	private ArrayList<LatLng> waypoints;

	WaypointStore() {
		waypoints = new ArrayList<>();
	}

	void addWaypoint(LatLng latLng) {
		waypoints.add(latLng);
	}

	void insertWaypoint(LatLng latLng, int index) {
		waypoints.add(index + 1, latLng);
	}

	void saveState(Bundle bundle) {
		bundle.putParcelableArrayList(WAYPOINT_KEY, waypoints);
	}

	void restoreState(Bundle bundle) {
		if (bundle != null && bundle.containsKey(WAYPOINT_KEY)) {
			waypoints = bundle.getParcelableArrayList(WAYPOINT_KEY);
		}
	}

	void removeWaypointAt(int index) {
		waypoints.remove(index);
	}

	LatLng[] getWaypointsArray() {
		LatLng[] result = new LatLng[waypoints.size()];
		return waypoints.toArray(result);
	}
}
