package au.com.criterionsoftware.waypoints;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	private static final String[] DEFAULT_POI_VALUES = new String[] { "airport" };
	private static final Set<String> DEFAULT_POI_VALUES_SET = new HashSet<>(Arrays.asList(DEFAULT_POI_VALUES));

	private GooglePlaces googlePlaces;
	private OnPlaceSearcherResult delegate;
	private Context context;

	private LatLng latLng;
	private boolean asInsert;
	private int index;

	PlaceSearcher(String googleMapsKey, OnPlaceSearcherResult delegate, Context context) {
		googlePlaces = new GooglePlaces(googleMapsKey);
		this.delegate = delegate;
		this.context = context;
	}

	@Override
	protected PlacesResult doInBackground(PlaceSearcherRequest... requests) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> poiList = sharedPreferences.getStringSet(context.getString(R.string.pref_poi_type_list), DEFAULT_POI_VALUES_SET);

		for (PlaceSearcherRequest request : requests) {
			try {
				latLng = request.latLng;
				asInsert = request.asInsert;
				index = request.index;

				return googlePlaces.getPlaces(new ArrayList<>(poiList), request.tolerance, request.latLng.latitude, request.latLng.longitude);
			} catch (Exception e) {
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(PlacesResult placesResult) {

		if (placesResult == null || placesResult.getStatusCode() != Result.StatusCode.OK) {
			delegate.onPlaceSearchResult(null, latLng, asInsert, index);
			return;
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
