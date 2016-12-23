package au.com.criterionsoftware.waypoints.googleplaces.query;

// Modified from https://github.com/gmarz/android-google-places

import android.location.Location;

public abstract class SearchQuery extends Query {

	private StringBuilder mTypes = new StringBuilder();

	public void setLocation(double latitude, double longitude) {
		String location = Double.toString(latitude) + "," + Double.toString(longitude);
		mQueryBuilder.addParameter("location", location);
	}

	public void setLocation(Location location) {
		setLocation(location.getLatitude(), location.getLongitude());
	}

	public void setRadius(int radius) {
		mQueryBuilder.addParameter("radius", Integer.toString(radius));
	}

	public void setKey(String key) {
		mQueryBuilder.addParameter("key", key);
	}

	public void addType(String type) {
		if (mTypes.length() > 0) {
			mTypes.append("|");
		}
		mTypes.append(type);
	}

	@Override
	public String toString() {
		mQueryBuilder.addParameter("types", mTypes.toString());
		return (getUrl() + mQueryBuilder.toString());
	}
}
