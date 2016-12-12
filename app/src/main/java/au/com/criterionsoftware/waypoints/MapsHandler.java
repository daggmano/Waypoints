package au.com.criterionsoftware.waypoints;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by darrenoster on 12/12/16.
 */

public class MapsHandler implements OnMapReadyCallback {

	private static final String LOG_TAG = MapsHandler.class.getSimpleName();

	private GoogleMap theMap;

	@Override
	public void onMapReady(GoogleMap googleMap) {
		theMap = googleMap;

		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		theMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		theMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
	}
}
