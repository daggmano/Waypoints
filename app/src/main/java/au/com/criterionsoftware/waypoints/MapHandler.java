package au.com.criterionsoftware.waypoints;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import au.com.criterionsoftware.waypoints.googleplaces.models.Place;
import au.com.criterionsoftware.waypoints.googleplaces.models.PlacesResult;

/**
 * Created by darrenoster on 12/12/16.
 */

interface OnShowWaypointDetail {
	void onShowWaypointDetail(int index, String name, LatLng latLng);
	boolean clearWaypointDetail();
}

class MapHandler implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, OnPlaceSearcherResult {

	private static final String LOG_TAG = MapHandler.class.getSimpleName();

	private static final String MAP_MODE_KEY = "mapMode";
	private static final String MAP_CAMERA_POSITION_KEY = "mapCameraPosition";
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
	private Marker distPoints[];

	private boolean isInDragMode;
	private int dragIndex;

	private Polyline nonDragPolylines[];
	private Polyline dragPolyline;
	private Marker dragPoint;
	private LatLng dragEndpoints[];

	private CameraPosition cameraPosition = null;

	MapHandler(Context context, WaypointStore waypointStore, OnShowWaypointDetail holder) {
		this.context = context;
		this.waypointStore = waypointStore;
		this.onShowWaypointDetailHolder = holder;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		theMap = googleMap;

		// Add a ic_marker in Sydney and move the camera
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
					confirmAddInsertWaypoint(latLng, false, 0);
				}
			}
		});

		theMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				if (isInDragMode) {
					return;
				}
				onShowWaypointDetailHolder.clearWaypointDetail();

				findClosestSegment(latLng);
			}
		});

		theMap.setOnMarkerClickListener(this);
		theMap.setOnMarkerDragListener(this);

		if (mapMode == MapMode.STAMEN) {
			switchToStamenMap();
		}

		if (cameraPosition != null) {
			theMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			cameraPosition = null;
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
				switchToMapMode(MapMode.STAMEN);
				break;

			case STAMEN:
				switchToMapMode(MapMode.GOOGLE);
				break;
		}
	}

	void saveState(Bundle bundle) {
		CameraPosition cameraPosition = theMap.getCameraPosition();

		bundle.putParcelable(MAP_CAMERA_POSITION_KEY, cameraPosition);
		bundle.putSerializable(MAP_MODE_KEY, mapMode);
	}

	void restoreState(Bundle bundle) {
		mapMode = MapMode.GOOGLE;

		if (bundle != null && bundle.containsKey(MAP_CAMERA_POSITION_KEY)) {
			cameraPosition = (CameraPosition) bundle.getParcelable(MAP_CAMERA_POSITION_KEY);

		}

		if (bundle != null && bundle.containsKey(MAP_MODE_KEY)) {
			mapMode = (MapMode) bundle.get(MAP_MODE_KEY);
		}

		if (theMap != null) {
			switchToMapMode(mapMode);
			if (cameraPosition != null) {
				theMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				cameraPosition = null;
			}
		}
	}

	private void switchToMapMode(MapMode mode) {
		mapMode = mode;

		switch (mapMode) {
			case STAMEN:
				switchToStamenMap();
				break;

			case GOOGLE:
				switchToGoogleMap();
				break;
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

	private void confirmAddInsertWaypoint(LatLng latLng, boolean asInsert, int index) {

		final float tolerance = 500 * (22 - theMap.getCameraPosition().zoom);

		PlaceSearcherRequest request = new PlaceSearcherRequest(latLng, (int) tolerance, asInsert, index);

		new PlaceSearcher(context.getString(R.string.google_maps_key), this, context).execute(request);
	}

	@Override
	public void onPlaceSearchResult(final PlacesResult placesResult, final LatLng latLng, final boolean asInsert, final int index) {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
		builderSingle.setTitle("Select Point:");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
		arrayAdapter.add("Manual Selection");
		if (placesResult != null) {
			for (Place p : placesResult.getPlaces()) {
				arrayAdapter.add(p.getName());
			}
		}

		builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				isInDragMode = false;
				redrawWaypoints();
			}
		});

		builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LatLng selectedLatLng;
				String selectedName;
				if (which == 0) {
					selectedLatLng = latLng;
					selectedName = "Manual Selection";
				} else {
					Place p = placesResult.getPlaces().get(which - 1);
					selectedLatLng = new LatLng(p.getLatitude(), p.getLongitude());
					selectedName = p.getName();
				}

				if (asInsert) {
					waypointStore.insertWaypoint(selectedLatLng, selectedName, index);
				} else {
					waypointStore.addWaypoint(selectedLatLng, selectedName);
				}
				isInDragMode = false;
				redrawWaypoints();
			}
		});
		builderSingle.show();
	}

	private void clearPolylines() {
		if (polyline != null) {
			polyline.remove();
			polyline = null;
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
	}

	private void clearMarkers() {
		if (mapPoints != null) {
			for (Marker m : mapPoints) {
				m.remove();
			}
			mapPoints = null;
		}
		if (dragPoint != null) {
			dragPoint.remove();
			dragPoint = null;
		}
		if (distPoints != null) {
			for (Marker m : distPoints) {
				m.remove();
			}
			distPoints = null;
		}
	}

	private enum MarkerType {
		NORMAL,
		START,
		END,
		DRAG,
		DISTANCE
	}

	private MarkerOptions createMarkerOptions(LatLng latLng, MarkerType type) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		int size = 60;

		switch (type) {
			case NORMAL:
				paint.setColor(Color.LTGRAY);
				break;
			case START:
				paint.setColor(Color.GREEN);
				break;
			case END:
				paint.setColor(Color.RED);
				break;
			case DRAG:
				paint.setColor(Color.YELLOW);
				break;
			case DISTANCE:
				paint.setColor(Color.BLUE);
				size = 20;
				break;
		}

		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawCircle(size / 2, size / 2, size / 2, paint);

		MarkerOptions options = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 0.5f);
		if (type == MarkerType.DRAG) {
			options.draggable(true);
		}

		return options;
	}

	void redrawWaypoints() {

		if (waypointStore == null || theMap == null) {
			return;
		}

		clearPolylines();
		clearMarkers();

		Waypoint[] waypoints = waypointStore.getWaypointsArray();

		mapPoints = new Marker[waypoints.length];

		PolylineOptions options = new PolylineOptions().color(Color.BLUE);
		for (int i = 0; i < waypoints.length; i++) {
			options.add(waypoints[i].latLng);

			MarkerOptions markerOptions = createMarkerOptions(waypoints[i].latLng, (i == 0) ? MarkerType.START : ((i == waypoints.length - 1) ? MarkerType.END : MarkerType.NORMAL));
			Marker m = theMap.addMarker(markerOptions);
			m.setTag(i);

			mapPoints[i] = m;
		}

		polyline = theMap.addPolyline(options);
		polyline.setZIndex(1000);

		addMilestones(waypoints);
	}

	private void addMilestones(Waypoint[] waypoints) {

		ArrayList<Marker> distMarkers = new ArrayList<>();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String distString = sharedPreferences.getString(context.getString(R.string.pref_dist_list), "km");
		float dist = 0f;
		switch (distString) {
			case "km":
				dist = 1000f;
				break;
			case "mi":
				dist = 1609.34f;
				break;
			case "nm":
				dist = 1852f;
				break;
		}

		if (dist == 0) {
			return;
		}

		double remainder = 0;

		for (int i = 1; i < waypoints.length; i++) {
			LatLng start = waypoints[i - 1].latLng;
			LatLng end = waypoints[i].latLng;

			double segmentLengthM = SphericalUtil.computeDistanceBetween(start, end);
			double segmentLength = segmentLengthM / dist;

			CalculatePointsResult pointsResult = DistanceBlipCalculator.calculatePoints(segmentLength, remainder);
			remainder = pointsResult.remainder;

			Log.d(LOG_TAG, "Points:");
			for (double p : pointsResult.points) {
				Log.d(LOG_TAG, "    " + p);
			}
			Log.d(LOG_TAG, "New Remainder: " + remainder);

			double latFactor = (end.latitude - start.latitude) / segmentLength;
			double lngFactor = (end.longitude - start.longitude) /segmentLength;

			for (double point : pointsResult.points) {
				double lat = start.latitude + (latFactor * point);
				double lng = start.longitude + (lngFactor * point);

				LatLng latLng = new LatLng(lat, lng);
				MarkerOptions markerOptions = createMarkerOptions(latLng, MarkerType.DISTANCE);
				distMarkers.add(theMap.addMarker(markerOptions));
			}
/*			if (offset > 0) {
				double firstSegmentM = dist - offset;

				if (firstSegmentM > segmentLengthM) {
					offset = firstSegmentM - segmentLengthM;
					continue;
				} else {
					double firstLat = (((end.latitude - start.latitude) / segmentLengthM) * firstSegmentM) + start.latitude;
					double firstLng = (((end.longitude - start.longitude) / segmentLengthM) * firstSegmentM) + start.longitude;

					LatLng firstPt = new LatLng(firstLat, firstLng);
					MarkerOptions firstOptions = createMarkerOptions(firstPt, MarkerType.DISTANCE);
					distMarkers.add(theMap.addMarker(firstOptions));

					start = firstPt;
					segmentLengthM = SphericalUtil.computeDistanceBetween(start, end);
				}
			}

			double segmentLength = segmentLengthM / dist;

			double deltaLat = (end.latitude - start.latitude) / segmentLength;
			double deltaLng = (end.longitude - start.longitude) / segmentLength;

			int steps = (int)Math.ceil(segmentLength);

			for (int j = 1; j < steps; j++) {
				LatLng pt = new LatLng(start.latitude + (deltaLat * j), start.longitude + (deltaLng * j));
				MarkerOptions markerOptions = createMarkerOptions(pt, MarkerType.DISTANCE);
				distMarkers.add(theMap.addMarker(markerOptions));
			}

			offset = dist * (steps - segmentLength);*/
		}

		distPoints = new Marker[distMarkers.size()];
		distPoints = distMarkers.toArray(distPoints);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		int i = (int) marker.getTag();

		Waypoint waypoint = waypointStore.getWaypointsArray()[i];
		onShowWaypointDetailHolder.onShowWaypointDetail(i, waypoint.name, waypoint.latLng);
		return true;
	}

	private void findClosestSegment(LatLng latLng) {
		Waypoint[] waypoints = waypointStore.getWaypointsArray();
		if (waypoints.length < 2) {
			return;
		}

		int closestIndex = -1;
		final float tolerance = 200 * (22 - theMap.getCameraPosition().zoom);

		for (int i = 0; i < waypoints.length - 1; i++) {
			List<LatLng> points = new ArrayList<>();
			points.add(waypoints[i].latLng);
			points.add(waypoints[i + 1].latLng);
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
		clearPolylines();

		if (distPoints != null) {
			for (Marker m : distPoints) {
				m.remove();
			}
			distPoints = null;
		}

		Waypoint[] waypoints = waypointStore.getWaypointsArray();
		// Set this up as we will need it shortly...
		dragEndpoints = new LatLng[] { waypoints[dragIndex].latLng, waypoints[dragIndex + 1].latLng };

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
				options.add(waypoints[i].latLng);
			}
			nonDragPolylines[idx] = theMap.addPolyline(options);
			nonDragPolylines[idx].setZIndex(1000);
			idx++;
		}

		if (dragIndex < waypoints.length - 2) {
			PolylineOptions options = new PolylineOptions().color(Color.BLUE);
			for (int i = dragIndex + 1; i < waypoints.length; i++) {
				options.add(waypoints[i].latLng);
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

		MarkerOptions markerOptions = createMarkerOptions(latLng, MarkerType.DRAG);

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
		confirmAddInsertWaypoint(marker.getPosition(), true, dragIndex);
	}
}
