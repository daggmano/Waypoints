package au.com.criterionsoftware.waypoints;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;

public class WaypointsActivity extends AppCompatActivity {

	private static final String LOG_TAG = WaypointsActivity.class.getSimpleName();

	private static final String WAYPOINTS_KEY = "editWaypoints";
	static final String EXTRA_WAYPOINTS = "waypointsBundle";

	private ArrayList<Waypoint> waypoints;
	private RecyclerView recyclerView;
	private WaypointsAdapter waypointsAdapter;

	private Paint paint = new Paint();

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

		waypointsAdapter = new WaypointsAdapter(waypoints);

		recyclerView = (RecyclerView) findViewById(R.id.waypoints_recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(waypointsAdapter);
		recyclerView.setHasFixedSize(true);

		ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
			@Override
			public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
				return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
			}

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				Collections.swap(waypoints, viewHolder.getAdapterPosition(), target.getAdapterPosition());
				waypointsAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
				return true;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				waypoints.remove(viewHolder.getAdapterPosition());
				waypointsAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
			}

			@Override
			public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
				Bitmap icon;
				if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
					View itemView = viewHolder.itemView;
					float height = (float) itemView.getBottom() - (float) itemView.getTop();
					float width = height / 3;

					if (dX < 0) {
						paint.setColor(getResources().getColor(R.color.colorDeleteRed));
						RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
						c.drawRect(background, paint);

						icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
						RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
						c.drawBitmap(icon, null, icon_dest, paint);
					}
				}

				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		};

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(recyclerView);

		Button btnCancel = (Button) findViewById(R.id.btn_cancel);
		Button btnAccept = (Button) findViewById(R.id.btn_accept);

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		btnAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList(WaypointStore.WAYPOINT_KEY, waypoints);
				intent.putExtra(WaypointsActivity.EXTRA_WAYPOINTS, bundle);

				setResult(RESULT_OK, intent);
				finish();
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
