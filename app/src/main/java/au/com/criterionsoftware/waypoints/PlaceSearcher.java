package au.com.criterionsoftware.waypoints;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import au.com.criterionsoftware.waypoints.googleplaces.GooglePlaces;
import au.com.criterionsoftware.waypoints.googleplaces.models.PlacesResult;
import au.com.criterionsoftware.waypoints.googleplaces.models.Result;

/**
 * Created by darrenoster on 16/12/16.
 */

interface OnPlaceSearcherResult {
	void onPlaceSearchResult(PlacesResult placesResult, LatLng latLng, boolean asInsert, int index);
}

class PlaceSearcher extends AsyncTask<PlaceSearcherRequest, Void, PlacesResult> {

	private final static String LOG_TAG = PlaceSearcher.class.getSimpleName();

	private GooglePlaces googlePlaces;
	private OnPlaceSearcherResult delegate;

	private LatLng latLng;
	private boolean asInsert;
	private int index;

	PlaceSearcher(String googleMapsKey, OnPlaceSearcherResult delegate) {
		googlePlaces = new GooglePlaces(googleMapsKey);
		this.delegate = delegate;
	}

	@Override
	protected PlacesResult doInBackground(PlaceSearcherRequest... requests) {
		for (PlaceSearcherRequest request : requests) {
			try {
				latLng = request.latLng;
				asInsert = request.asInsert;
				index = request.index;

				return googlePlaces.getPlaces("airport", request.tolerance, request.latLng.latitude, request.latLng.longitude);
			} catch (Exception e) {
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(PlacesResult placesResult) {

		if (placesResult == null || placesResult.getStatusCode() != Result.StatusCode.OK) {
			delegate.onPlaceSearchResult(null, latLng, asInsert, index);
		}
		delegate.onPlaceSearchResult(placesResult, latLng, asInsert, index);
	}
}

class PlaceSearcherRequest {
	LatLng latLng;
	int tolerance;
	boolean asInsert;
	int index;

	PlaceSearcherRequest(LatLng latLng, int tolerance, boolean asInsert, int index) {
		this.latLng = latLng;
		this.tolerance = tolerance;
		this.asInsert = asInsert;
		this.index = index;
	}
}
