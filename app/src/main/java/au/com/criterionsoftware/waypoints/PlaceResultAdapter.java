package au.com.criterionsoftware.waypoints;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by darrenoster on 2/1/17.
 */

class PlaceResultAdapter extends BaseAdapter {

	private ArrayList<PlaceResultChoiceItem> _data;
	private Context _context;

	PlaceResultAdapter(ArrayList<PlaceResultChoiceItem> data, Context context) {
		_data = data;
		_context = context;
	}

	@Override
	public int getCount() {
		return _data.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	private class ViewHolder {
		TextView titleView;
		TextView latLngView;
		TextView distanceView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_place_result, null);

			viewHolder = new ViewHolder();
			viewHolder.titleView = (TextView) convertView.findViewById(R.id.place_result_title);
			viewHolder.latLngView = (TextView) convertView.findViewById(R.id.place_result_latlng);
			viewHolder.distanceView = (TextView) convertView.findViewById(R.id.place_result_distance);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		PlaceResultChoiceItem item = _data.get(position);

		viewHolder.titleView.setText(item.getTitle());
		viewHolder.latLngView.setText(item.getLatLngString());
		viewHolder.distanceView.setText(item.getDistance());

		return convertView;
	}
}
