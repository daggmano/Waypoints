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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.PolyUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by darrenoster on 12/12/16.
 */

interface OnShowWaypointDetail {
	void onShowWaypointDetail(int index, LatLng latLng);
	boolean clearWaypointDetail();
}

class MapHandler implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

	private static final String LOG_TAG = MapHandler.class.getSimpleName();

	private static final String MAP_MODE_KEY = "mapMode";
	private static final String STAMEN_BASE_URL = "http://tile.stamen.com/watercolor/%d/%d/%d.jpg";

	private Context context;
	private WaypointStore waypointStore;

	private GoogleMap theMap;
	private TileOverlay tileOverlay;

	private OnShowWaypointDetail onShowWaypointDetailHolder;

	private enum MapMode {
		GOOGLE, STAMEN
	}

	private MapMode mapMode;

	private Polyline polyline;
	private Marker mapPoints[];

	private boolean isInDragMode;
	private int dragIndex;

	private Polyline nonDragPolylines[];
	private Polyline dragPolyline;
	private Marker dragPoint;
	private LatLng dragEndpoints[];

	MapHandler(Context context, WaypointStore waypointStore, OnShowWaypointDetail holder) {
		this.context = context;
		this.waypointStore = waypointStore;
		this.onShowWaypointDetailHolder = holder;
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
				if (isInDragMode) {
					isInDragMode = false;
					redrawWaypoints();
					return;
				}
				if (!onShowWaypointDetailHolder.clearWaypointDetail()) {
					confirmAddWaypoint(latLng);
				}
			}
		});

		theMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				onShowWaypointDetailHolder.clearWaypointDetail();

				findClosestSegment(latLng);
			}
		});

		theMap.setOnMarkerClickListener(this);
		theMap.setOnMarkerDragListener(this);

		if (mapMode == MapMode.STAMEN) {
			switchToStamenMap();
		}

		isInDragMode = false;
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
		if (nonDragPolylines != null) {
			for (Polyline p : nonDragPolylines) {
				p.remove();
			}
			nonDragPolylines = null;
		}
		if (dragPolyline != null) {
			dragPolyline.remove();
			dragPolyline = null;
		}

		if (mapPoints != null) {
			for (Marker m : mapPoints) {
				m.remove();
			}
		}
		if (dragPoint != null) {
			dragPoint.remove();
			dragPoint = null;
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

		onShowWaypointDetailHolder.onShowWaypointDetail(i, waypointStore.getWaypointsArray()[i]);
		return true;
	}

	private void findClosestSegment(LatLng latLng) {
		LatLng[] waypoints = waypointStore.getWaypointsArray();
		if (waypoints.length < 2) {
			return;
		}

		int closestIndex = -1;
		final float tolerance = 200 * (22 - theMap.getCameraPosition().zoom);

		for (int i = 0; i < waypoints.length - 1; i++) {
			List<LatLng> points = new ArrayList<>();
			points.add(waypoints[i]);
			points.add(waypoints[i + 1]);
			if (PolyUtil.isLocationOnPath(latLng, points, false, tolerance)) {
				closestIndex = i;
			}
		}

		if (closestIndex != -1) {
			startDragMode(closestIndex, latLng);
		}
	}

	private void startDragMode(int index, LatLng latLng) {
		isInDragMode = true;
		dragIndex = index;

		redrawNonDragWaypoints();
		initDragWaypoints(latLng);
	}

	void redrawNonDragWaypoints() {
		if (polyline != null) {
			polyline.remove();
			polyline = null;
		}
		if (nonDragPolylines != null) {
			for (Polyline p : nonDragPolylines) {
				p.remove();
			}
		}

		LatLng[] waypoints = waypointStore.getWaypointsArray();
		// Set this up as we will need it shortly...
		dragEndpoints = new LatLng[] { waypoints[dragIndex], waypoints[dragIndex + 1] };

		// Do we need more than one nonDragPolyline?
		if (dragIndex != 0 && dragIndex < waypoints.length - 2) {
			nonDragPolylines = new Polyline[2];
		} else if (waypoints.length > 2) {
			nonDragPolylines = new Polyline[1];
		} else {
			nonDragPolylines = null;
		}

		int idx = 0;
		if (dragIndex > 0) {
			PolylineOptions options = new PolylineOptions().color(Color.BLUE);
			for (int i = 0; i <= dragIndex; i++) {
				options.add(waypoints[i]);
			}
			nonDragPolylines[idx] = theMap.addPolyline(options);
			nonDragPolylines[idx].setZIndex(1000);
			idx++;
		}

		if (dragIndex < waypoints.length - 2) {
			PolylineOptions options = new PolylineOptions().color(Color.BLUE);
			for (int i = dragIndex + 1; i < waypoints.length; i++) {
				options.add(waypoints[i]);
			}
			nonDragPolylines[idx] = theMap.addPolyline(options);
			nonDragPolylines[idx].setZIndex(1000);
		}
	}

	private void initDragWaypoints(LatLng latLng) {
		PolylineOptions options = new PolylineOptions()
				.color(Color.YELLOW)
				.add(dragEndpoints[0], latLng, dragEndpoints[1]);
		dragPolyline = theMap.addPolyline(options);
		dragPolyline.setZIndex(1000);

		MarkerOptions markerOptions = new MarkerOptions()
				.position(latLng)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
				.draggable(true);

		dragPoint = theMap.addMarker(markerOptions);
	}

	private void redrawDragWaypoints(LatLng latLng) {
		if (!isInDragMode) {
			return;
		}

		if (dragPolyline != null) {
			dragPolyline.remove();
		}

		PolylineOptions options = new PolylineOptions()
				.color(Color.YELLOW)
				.add(dragEndpoints[0], latLng, dragEndpoints[1]);
		dragPolyline = theMap.addPolyline(options);
		dragPolyline.setZIndex(1000);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {

	}

	@Override
	public void onMarkerDrag(Marker marker) {
		redrawDragWaypoints(marker.getPosition());
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		final LatLng latLng = marker.getPosition();

		String title = "Confirm Action";
		String message = String.format(Locale.getDefault(), "Do you wish to insert the following waypoint?\r\nLat: %f\r\nLng: %f", latLng.latitude, latLng.longitude);

		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						waypointStore.insertWaypoint(latLng, dragIndex);
						isInDragMode = false;
						redrawWaypoints();
					}})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						isInDragMode = false;
						redrawWaypoints();
					}
				}).show();
	}
}
