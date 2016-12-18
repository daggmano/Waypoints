package au.com.criterionsoftware.waypoints;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by darrenoster on 13/12/16.
 */

class WaypointViewHolder extends RecyclerView.ViewHolder {

	TextView waypointNameTextView;
	TextView waypointLatLngTextView;

	WaypointViewHolder(View itemView) {
		super(itemView);

		waypointNameTextView = (TextView) itemView.findViewById(R.id.waypoint_text);
		waypointLatLngTextView = (TextView) itemView.findViewById(R.id.waypoint_latlng);
	}
}
