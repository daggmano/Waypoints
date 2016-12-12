package au.com.criterionsoftware.waypoints;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by darrenoster on 12/12/16.
 */

public class MapHandler implements OnMapReadyCallback {

	private static final String LOG_TAG = MapHandler.class.getSimpleName();

	private static final String MAP_MODE_KEY = "mapMode";
	private static final String STAMEN_BASE_URL = "http://tile.stamen.com/watercolor/%d/%d/%d.jpg";

	private GoogleMap theMap;
	private TileOverlay tileOverlay;

	private enum MapMode {
		GOOGLE, STAMEN
	}

	private MapMode mapMode;

	@Override
	public void onMapReady(GoogleMap googleMap) {
		theMap = googleMap;
		if (mapMode == MapMode.STAMEN) {
			switchToStamenMap();
		}

		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		theMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		theMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

		theMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				Log.d(LOG_TAG, "Click!");
			}
		});

		theMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				Log.d(LOG_TAG, "Long click!");
			}
		});
	}

	void toggleMapDisplay() {
		if (theMap == null) {
			return;
		}

		switch (mapMode) {
			case GOOGLE:
				mapMode = MapMode.STAMEN;
				switchToStamenMap();
				break;

			case STAMEN:
				mapMode = MapMode.GOOGLE;
				switchToGoogleMap();
				break;
		}
	}

	void saveState(Bundle bundle) {
		bundle.putSerializable(MAP_MODE_KEY, mapMode);
	}

	void restoreState(Bundle bundle) {
		mapMode = MapMode.GOOGLE;
		if (bundle != null && bundle.containsKey(MAP_MODE_KEY)) {
			mapMode = (MapMode) bundle.get(MAP_MODE_KEY);
		}
	}

	private void switchToStamenMap() {
		theMap.setMapType(GoogleMap.MAP_TYPE_NONE);

		TileOverlayOptions options = new TileOverlayOptions();
		options.tileProvider(new UrlTileProvider(256, 256) {
			@Override
			public URL getTileUrl(int x, int y, int z) {
				try {
					return new URL(String.format(Locale.getDefault(), STAMEN_BASE_URL, z, x, y));
				}
				catch (MalformedURLException e) {
					return null;
				}
			}
		});

		tileOverlay = theMap.addTileOverlay(options);
	}

	private void switchToGoogleMap() {
		theMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		if (tileOverlay != null) {
			tileOverlay.remove();
		}
	}
}
