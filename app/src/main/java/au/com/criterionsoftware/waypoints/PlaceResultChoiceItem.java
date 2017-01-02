package au.com.criterionsoftware.waypoints;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Created by darrenoster on 2/1/17.
 */

class PlaceResultChoiceItem {
	private String _title;
	private LatLng _latLng;
	private String _distance;

	PlaceResultChoiceItem(String title, LatLng latLng, String distance) {
		_title = title;
		_latLng = latLng;
		_distance = distance;
	}

	String getTitle() {
		return _title;
	}

	LatLng getLatLng() {
		return _latLng;
	}

	String getLatLngString() {
		return String.format(Locale.getDefault(), "%.6f, %.6f", _latLng.latitude, _latLng.longitude);
	}

	String getDistance() {
		return _distance;
	}
}
