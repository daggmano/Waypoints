package au.com.criterionsoftware.waypoints;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnShowWaypointDetail, OnWaypointSummaryChanged {

	private final String LOG_TAG = MainActivity.class.getSimpleName();

	private final int EDIT_WAYPOINTS_REQUEST = 1;

	private MapHandler mapHandler;
	private WaypointStore waypointStore;

	private TextView toolbarInfo;

	private CardView waypointInfoCard;
	private TextView waypointInfoTitle;
	private TextView waypointInfoName;
	private TextView waypointInfoLatLng;

	private int waypointInfoIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbarInfo = (TextView) findViewById(R.id.toolbar_info);

		waypointInfoIndex = -1;

		waypointInfoCard = (CardView) findViewById(R.id.waypoint_info_card);
		waypointInfoTitle = (TextView) findViewById(R.id.waypoint_info_title);
		waypointInfoName = (TextView) findViewById(R.id.waypoint_info_name);
		waypointInfoLatLng = (TextView) findViewById(R.id.waypoint_info_latlng);
		Button waypointInfoDelete = (Button) findViewById(R.id.waypoint_info_delete);

		waypointInfoDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (waypointInfoIndex > -1) {
					waypointInfoCard.setVisibility(View.GONE);
					waypointStore.removeWaypointAt(waypointInfoIndex);
					mapHandler.redrawWaypoints();
					waypointInfoIndex = -1;
				}
			}
		});

		waypointStore = new WaypointStore(this);
		mapHandler = new MapHandler(this, waypointStore, this);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(mapHandler);

		waypointStore.restoreState(savedInstanceState);
		mapHandler.restoreState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.menu_toggle_map:
				mapHandler.toggleMapDisplay();
				break;

			case R.id.menu_edit_waypoints:
				editWaypoints();
				break;

			case R.id.menu_preferences:
				startActivity(new Intent(this, PreferencesActivity.class));
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		mapHandler.saveState(outState);
		waypointStore.saveState(outState);

		super.onSaveInstanceState(outState);
	}

	private void editWaypoints() {
		Intent intent = new Intent(this, WaypointsActivity.class);

		Bundle bundle = new Bundle();
		waypointStore.saveState(bundle);
		intent.putExtra(WaypointsActivity.EXTRA_WAYPOINTS, bundle);

		startActivityForResult(intent, EDIT_WAYPOINTS_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_WAYPOINTS_REQUEST) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getBundleExtra(WaypointsActivity.EXTRA_WAYPOINTS);
				waypointStore.restoreState(bundle);
				mapHandler.redrawWaypoints();
			}
		}
	}

	@Override
	public void onShowWaypointDetail(int index, String name, LatLng latLng) {
		waypointInfoIndex = index;
		waypointInfoTitle.setText(String.format(Locale.getDefault(), "WAYPOINT %d", (index + 1)));
		waypointInfoName.setText(name);
		waypointInfoLatLng.setText(String.format(Locale.getDefault(), "Lat / Lng: %f / %f", latLng.latitude, latLng.longitude));
		waypointInfoCard.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean clearWaypointDetail() {
		if (waypointInfoCard.getVisibility() == View.VISIBLE) {
			waypointInfoCard.setVisibility(View.GONE);
			waypointInfoIndex = -1;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onWaypointSummaryChange(int waypointCount, int distance) {
		float km = (float) distance / 1000;
		toolbarInfo.setText(String.format(Locale.getDefault(), "%d points, %.2f km", waypointCount, km));
	}
}
