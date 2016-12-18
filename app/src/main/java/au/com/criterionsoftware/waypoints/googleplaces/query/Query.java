package au.com.criterionsoftware.waypoints.googleplaces.query;

// Modified from https://github.com/gmarz/android-google-places

public abstract class Query {

	public QueryBuilder mQueryBuilder = new QueryBuilder();

	public Query() {
		setSensor(false); // Default
	}

	public void setSensor(boolean sensor) {
		mQueryBuilder.addParameter("sensor", Boolean.toString(sensor));
	}

	public void setLanguage(String language) {
		mQueryBuilder.addParameter("language", language);
	}

	public abstract String getUrl();

	@Override
	public String toString() {
		return (getUrl() + mQueryBuilder.toString());
	}
}
