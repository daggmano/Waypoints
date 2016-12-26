package au.com.criterionsoftware.waypoints;

import java.util.ArrayList;

/**
 * Created by darrenoster on 26/12/16.
 */

class DistanceBlipCalculator {

	static CalculatePointsResult calculatePoints(double totalLength, double remainder) {
		ArrayList<Double> points = new ArrayList<>();
		CalculatePointsResult result = new CalculatePointsResult();

		if (totalLength < remainder) {
			result.remainder = remainder - totalLength;
			result.points = new double[0];
			return result;
		}

		double l = totalLength;
		double next = remainder + 1;
		if (remainder != 0) {
			points.add(remainder);
		}
		l -= remainder;

		while (l >= 1) {
			points.add(next++);
			--l;
		}

		if (l > 0.001) {
			result.remainder = 1 - l;
		} else {
			result.remainder = 0;
		}

		result.points = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			result.points[i] = points.get(i);
		}

		return result;
	}
}

class CalculatePointsResult {
	double[] points;
	double remainder;
}
