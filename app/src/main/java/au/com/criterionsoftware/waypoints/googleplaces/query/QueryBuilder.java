package au.com.criterionsoftware.waypoints.googleplaces.query;

// Modified from https://github.com/gmarz/android-google-places

import android.content.ContentValues;

import java.util.Map;

public class QueryBuilder {

	private ContentValues mParameters = new ContentValues();

	public void addParameter(String name, String value) {
		removeParameter(name);
		mParameters.put(name, value);
	}

	public void removeParameter(String name) {
		if (mParameters.containsKey(name)) {
			mParameters.remove(name);
		}
	}

	public void clearParameters() {
		mParameters.clear();
	}

	public String toString() {
		StringBuilder query = new StringBuilder();

		query.append("?");

		for (Map.Entry<String, Object> entry : mParameters.valueSet()) {
			query.append(entry.getKey());
			query.append("=");
			query.append(entry.getValue());
			query.append("&");
		}

		return encode(query.toString());
	}

	private String encode(String query) {
		String encodedQuery = query.replace("|", "%7C");
		encodedQuery = encodedQuery.replace(' ', '+');

		return encodedQuery;
	}
}
