package au.com.criterionsoftware.waypoints;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by darrenoster on 2/1/17.
 */

class DistanceUnit {

	private String unitText;
	private float metricLength;

	String getUnitText() {
		return unitText;
	}

	float getMetricLength() {
		return metricLength;
	}

	static DistanceUnit getValue(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String prefValue = sharedPreferences.getString(context.getString(R.string.pref_dist_list), context.getString(R.string.default_pref_dist_list));

		DistanceUnit result = new DistanceUnit();
		result.metricLength = 0;
		result.unitText = "";

		switch (prefValue) {
			case "km":
				result.metricLength = 1000f;
				result.unitText = "km";
				break;
			case "mi":
				result.metricLength = 1609.34f;
				result.unitText = "mi";
				break;
			case "nm":
				result.metricLength = 1852f;
				result.unitText = "NM";
				break;
		}

		return result;
	}
}
