package au.com.criterionsoftware.waypoints;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

public class WaypointsActivity extends AppCompatActivity {

	private static final String LOG_TAG = WaypointsActivity.class.getSimpleName();

	private static final String WAYPOINTS_KEY = "editWaypoints";
	static final String EXTRA_WAYPOINTS = "waypointsBundle";

	private ArrayList<LatLng> waypoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waypoints);

		setTitle(R.string.edit_waypoints);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (savedInstanceState != null && savedInstanceState.containsKey(WAYPOINTS_KEY)) {
			waypoints = savedInstanceState.getParcelableArrayList(WAYPOINTS_KEY);
		} else {
			Bundle waypointsBundle = getIntent().getBundleExtra(EXTRA_WAYPOINTS);
			waypoints = waypointsBundle.getParcelableArrayList(WaypointStore.WAYPOINT_KEY);
		}

		if (waypoints == null) {
			waypoints = new ArrayList<>();
		}


		ArrayList<String> sa = new ArrayList<>();
		int i = 1;
		for (LatLng latLng : waypoints) {
			sa.add(String.format(Locale.getDefault(), "%d. %f, %f", i++, latLng.latitude, latLng.longitude));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sa);
		ListView lv = (ListView)findViewById(R.id.list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id)
			{
				Toast.makeText(getBaseContext(), "Click, position = " + position, Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(WAYPOINTS_KEY, waypoints);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		Log.d(LOG_TAG, "onBackPressed");
		super.onBackPressed();
	}
}
