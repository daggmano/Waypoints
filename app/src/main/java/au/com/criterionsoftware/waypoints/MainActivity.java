package au.com.criterionsoftware.waypoints;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	private final String LOG_TAG = MainActivity.class.getSimpleName();

	private final int EDIT_WAYPOINTS_REQUEST = 1;

	private MapHandler mapHandler;
	private WaypointStore waypointStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		waypointStore = new WaypointStore();
		mapHandler = new MapHandler(this, waypointStore);

		waypointStore.restoreState(savedInstanceState);
		mapHandler.restoreState(savedInstanceState);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(mapHandler);
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
			case R.id.toggle_map:
				mapHandler.toggleMapDisplay();
				break;

			case R.id.edit_waypoints:
				editWaypoints();
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
			}
		}
	}
}
