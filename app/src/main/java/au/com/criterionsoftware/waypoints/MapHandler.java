package au.com.criterionsoftware.waypoints;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by darrenoster on 12/12/16.
 */

class MapHandler implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

	private static final String LOG_TAG = MapHandler.class.getSimpleName();

	private static final String MAP_MODE_KEY = "mapMode";
	private static final String STAMEN_BASE_URL = "http://tile.stamen.com/watercolor/%d/%d/%d.jpg";

	private Context context;
	private WaypointStore waypointStore;

	private GoogleMap theMap;
	private TileOverlay tileOverlay;

	private enum MapMode {
		GOOGLE, STAMEN
	}

	private MapMode mapMode;

	private Polyline polyline;
	private Marker mapPoints[];

	MapHandler(Context context, WaypointStore waypointStore) {
		this.context = context;
		this.waypointStore = waypointStore;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		theMap = googleMap;

		// Add a marker in Sydney and move the camera
		LatLng adl = new LatLng(-34.9285, 138.6007);
		theMap.moveCamera(CameraUpdateFactory.newLatLngZoom(adl, 9.0f));

		theMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				confirmAddWaypoint(latLng);
			}
		});

		theMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				Log.d(LOG_TAG, "Long click!");
			}
		});

		theMap.setOnMarkerClickListener(this);

		if (mapMode == MapMode.STAMEN) {
			switchToStamenMap();
		}

		redrawWaypoints();
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

		if (theMap != null) {
			switch (mapMode) {
				case STAMEN:
					switchToStamenMap();
					break;

				case GOOGLE:
					switchToGoogleMap();
					break;
			}
		}
	}

	private void confirmAddWaypoint(final LatLng latLng) {

		String title = "Confirm Action";
		String message = String.format(Locale.getDefault(), "Do you wish to add the following waypoint?\r\nLat: %f\r\nLng: %f", latLng.latitude, latLng.longitude);

		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						waypointStore.addWaypoint(latLng);
						redrawWaypoints();
					}})
				.setNegativeButton(android.R.string.no, null).show();
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

	void redrawWaypoints() {
		if (polyline != null) {
			polyline.remove();
		}

		if (mapPoints != null) {
			for (Marker m : mapPoints) {
				m.remove();
			}
		}

		LatLng[] waypoints = waypointStore.getWaypointsArray();

		mapPoints = new Marker[waypoints.length];

		PolylineOptions options = new PolylineOptions().color(Color.BLUE);
		int i = 0;
		for (LatLng latLng : waypoints) {
			options.add(latLng);

			float markerColor = i == 0
					? BitmapDescriptorFactory.HUE_GREEN
					: i == waypoints.length - 1
						? BitmapDescriptorFactory.HUE_RED
						: BitmapDescriptorFactory.HUE_BLUE;


			MarkerOptions mo = new MarkerOptions()
					.position(latLng)
					.icon(BitmapDescriptorFactory.defaultMarker(markerColor));

			Marker m = theMap.addMarker(mo);
			m.setTag(i);

			mapPoints[i++] = m;
		}

		polyline = theMap.addPolyline(options);
		polyline.setZIndex(1000);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		int i = (int) marker.getTag();

		Log.d(LOG_TAG, "Clicked marker at " + waypointStore.getWaypointsArray()[i]);
		return true;
	}
}
