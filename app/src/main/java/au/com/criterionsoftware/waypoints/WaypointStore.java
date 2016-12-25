package au.com.criterionsoftware.waypoints;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

/**
 * Created by darrenoster on 12/12/16.
 */

interface OnWaypointSummaryChanged {
	void onWaypointSummaryChange(int waypointCount, int distance);
}

class Waypoint implements Parcelable {
	LatLng latLng;
	String name;

	Waypoint(LatLng latLng, String name) {
		this.latLng = latLng;
		this.name = name;
	}

	private Waypoint(Parcel parcel) {
		latLng = parcel.readParcelable(LatLng.class.getClassLoader());
		name = parcel.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeParcelable(latLng, i);
		parcel.writeString(name);
	}

	public static final Parcelable.Creator<Waypoint> CREATOR = new Parcelable.Creator<Waypoint>() {

		@Override
		public Waypoint createFromParcel(Parcel in) {
			return new Waypoint(in);
		}

		@Override
		public Waypoint[] newArray(int size) {
			return new Waypoint[size];
		}
	};
}

class WaypointStore {

	private static final String LOG_TAG = WaypointStore.class.getSimpleName();

	static final String WAYPOINT_KEY = "waypoints";

	private ArrayList<Waypoint> waypoints;

	private OnWaypointSummaryChanged delegate;

	WaypointStore(OnWaypointSummaryChanged delegate) {
		this.delegate = delegate;

		waypoints = new ArrayList<>();
		updateSummary();
	}

	void addWaypoint(LatLng latLng, String name) {
		Waypoint w = new Waypoint(latLng, name);

		waypoints.add(w);
		updateSummary();
	}

	void insertWaypoint(LatLng latLng, String name, int index) {
		Waypoint w = new Waypoint(latLng, name);

		waypoints.add(index + 1, w);
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

	Waypoint[] getWaypointsArray() {
		Waypoint[] result = new Waypoint[waypoints.size()];
		return waypoints.toArray(result);
	}

	void updateSummary() {
		if (waypoints.size() < 2) {
			delegate.onWaypointSummaryChange(waypoints.size(), 0);
		}

		float totalDistance = 0;
		for (int i = 1; i < waypoints.size(); i++) {
			totalDistance += SphericalUtil.computeDistanceBetween(waypoints.get(i - 1).latLng, waypoints.get(i).latLng);
		}

		delegate.onWaypointSummaryChange(waypoints.size(), (int) totalDistance);
	}
}
