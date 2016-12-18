package au.com.criterionsoftware.waypoints;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by darrenoster on 13/12/16.
 */

public class WaypointsAdapter extends RecyclerView.Adapter {//implements CardView.OnLongClickListener {

	private ArrayList<Waypoint> waypoints;

	WaypointsAdapter(ArrayList<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_waypoint, parent, false);

		return new WaypointViewHolder(layout);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		WaypointViewHolder viewHolder = (WaypointViewHolder) holder;

		viewHolder.waypointNameTextView.setText(waypoints.get(position).name);
		viewHolder.waypointLatLngTextView.setText(String.format(Locale.getDefault(), "%f, %f", waypoints.get(position).latLng.latitude, waypoints.get(position).latLng.longitude));
	}

	@Override
	public int getItemCount() {
		return waypoints.size();
	}
}
