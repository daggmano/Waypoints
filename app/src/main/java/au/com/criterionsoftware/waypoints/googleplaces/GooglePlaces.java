package au.com.criterionsoftware.waypoints.googleplaces;

// Modified from https://github.com/gmarz/android-google-places

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import au.com.criterionsoftware.waypoints.googleplaces.models.DetailsResult;
import au.com.criterionsoftware.waypoints.googleplaces.models.PlacesResult;
import au.com.criterionsoftware.waypoints.googleplaces.query.DetailsQuery;
import au.com.criterionsoftware.waypoints.googleplaces.query.NearbySearchQuery;
import au.com.criterionsoftware.waypoints.googleplaces.query.Query;
import au.com.criterionsoftware.waypoints.googleplaces.query.TextSearchQuery;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlaces {

	private String mApiKey = "";
	private AbstractSet<String> mSupportedPlaces;

	public GooglePlaces(String apiKey) {
		mApiKey = apiKey;
		loadSupportedPlaces();
	}

	public PlacesResult getPlaces(List<String> types, String keyword, int radius, double lat, double lon)
			throws JSONException, IOException {
		NearbySearchQuery query = new NearbySearchQuery(lat, lon);

		query.setRadius(radius);

		if (types != null) {
			for(String type : types){
				query.addType(type);
			}
		}

		if (keyword != null && keyword != "") {
			query.setKeyword(keyword);
		}

		PlacesResult result = getPlaces(query);

		return result;
	}

	public PlacesResult getPlaces(List<String> types, int radius, double lat, double lon)
			throws JSONException, IOException {
		return getPlaces(types, null, radius, lat, lon);
	}

	public PlacesResult getPlaces(String type, String keyword, int radius, double lat, double lon)
			throws JSONException, IOException {
		List<String> types = Arrays.asList(type);
		return getPlaces(types, keyword, radius, lat, lon);
	}

	public PlacesResult getPlaces(String type, int radius, double lat, double lon)
			throws JSONException, IOException {
		return getPlaces(type, null, radius, lat, lon);
	}

	public PlacesResult getPlaces(String searchText, double lat, double lon)
			throws JSONException, IOException {
		TextSearchQuery query = new TextSearchQuery(searchText);
		query.setLocation(lat, lon);
		PlacesResult result = getPlaces(query);

		return result;
	}

	public PlacesResult getPlaces(String searchText)
			throws JSONException, IOException {
		TextSearchQuery query = new TextSearchQuery(searchText);
		PlacesResult result = getPlaces(query);

		return result;
	}

	public PlacesResult getPlaces(Query query)
			throws JSONException, IOException {
		JSONObject response = executeRequest(query.toString());
		PlacesResult result = new PlacesResult(response);

		return result;
	}

	public DetailsResult getPlaceDetails(String reference)
			throws JSONException, IOException {
		DetailsQuery query = new DetailsQuery(reference);
		DetailsResult result = getPlaceDetails(query);

		return result;
	}

	public DetailsResult getPlaceDetails(Query query)
			throws JSONException, IOException {
		JSONObject response = executeRequest(query.toString());
		DetailsResult result = new DetailsResult(response);

		return result;
	}

	public boolean isSupportedPlace(String placeType) {
		return (mSupportedPlaces.contains(placeType));
	}

	private JSONObject executeRequest(String query)
			throws IOException, JSONException {
		query += "key=" + mApiKey;

		URL url = new URL(query);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line);
		}

		urlConnection.disconnect();

		JSONObject jsonResponse = new JSONObject(result.toString());

		return jsonResponse;
	}

	private void loadSupportedPlaces() {
		mSupportedPlaces = new HashSet<>();

		mSupportedPlaces.add("accounting");
		mSupportedPlaces.add("airport");
		mSupportedPlaces.add("amusement park");
		mSupportedPlaces.add("aquarium");
		mSupportedPlaces.add("art gallery");
		mSupportedPlaces.add("atm");
		mSupportedPlaces.add("bakery");
		mSupportedPlaces.add("bank");
		mSupportedPlaces.add("bar");
		mSupportedPlaces.add("beauty salon");
		mSupportedPlaces.add("bicycle store");
		mSupportedPlaces.add("book store");
		mSupportedPlaces.add("bowling alley");
		mSupportedPlaces.add("bus station");
		mSupportedPlaces.add("cafe");
		mSupportedPlaces.add("campground");
		mSupportedPlaces.add("car dealer");
		mSupportedPlaces.add("car rental");
		mSupportedPlaces.add("car repair");
		mSupportedPlaces.add("car wash");
		mSupportedPlaces.add("casino");
		mSupportedPlaces.add("cemetery");
		mSupportedPlaces.add("church");
		mSupportedPlaces.add("city hall");
		mSupportedPlaces.add("clothing store");
		mSupportedPlaces.add("convenience store");
		mSupportedPlaces.add("courthouse");
		mSupportedPlaces.add("dentist");
		mSupportedPlaces.add("department store");
		mSupportedPlaces.add("doctor");
		mSupportedPlaces.add("electrician");
		mSupportedPlaces.add("electronics store");
		mSupportedPlaces.add("embassy");
		mSupportedPlaces.add("establishment");
		mSupportedPlaces.add("finance");
		mSupportedPlaces.add("fire station");
		mSupportedPlaces.add("florist");
		mSupportedPlaces.add("food");
		mSupportedPlaces.add("funeral home");
		mSupportedPlaces.add("furniture store");
		mSupportedPlaces.add("gas station");
		mSupportedPlaces.add("general contractor");
		mSupportedPlaces.add("grocery or supermarket");
		mSupportedPlaces.add("gym");
		mSupportedPlaces.add("hair care");
		mSupportedPlaces.add("hardware store");
		mSupportedPlaces.add("health");
		mSupportedPlaces.add("hindu temple");
		mSupportedPlaces.add("home goods store");
		mSupportedPlaces.add("hospital");
		mSupportedPlaces.add("insurance agency");
		mSupportedPlaces.add("jewelry store");
		mSupportedPlaces.add("laundry");
		mSupportedPlaces.add("lawyer");
		mSupportedPlaces.add("library");
		mSupportedPlaces.add("liquor store");
		mSupportedPlaces.add("local government office");
		mSupportedPlaces.add("locksmith");
		mSupportedPlaces.add("lodging");
		mSupportedPlaces.add("meal delivery");
		mSupportedPlaces.add("meal takeaway");
		mSupportedPlaces.add("mosque");
		mSupportedPlaces.add("movie rental");
		mSupportedPlaces.add("movie theater");
		mSupportedPlaces.add("moving company");
		mSupportedPlaces.add("museum");
		mSupportedPlaces.add("night club");
		mSupportedPlaces.add("painter");
		mSupportedPlaces.add("park");
		mSupportedPlaces.add("parking");
		mSupportedPlaces.add("pet store");
		mSupportedPlaces.add("pharmacy");
		mSupportedPlaces.add("physiotherapist");
		mSupportedPlaces.add("place of worship");
		mSupportedPlaces.add("plumber");
		mSupportedPlaces.add("police");
		mSupportedPlaces.add("post office");
		mSupportedPlaces.add("real estate agency");
		mSupportedPlaces.add("restaurant");
		mSupportedPlaces.add("roofing contractor");
		mSupportedPlaces.add("rv park");
		mSupportedPlaces.add("school");
		mSupportedPlaces.add("shoe store");
		mSupportedPlaces.add("shopping mall");
		mSupportedPlaces.add("spa");
		mSupportedPlaces.add("stadium");
		mSupportedPlaces.add("storage");
		mSupportedPlaces.add("store");
		mSupportedPlaces.add("subway station");
		mSupportedPlaces.add("synagogue");
		mSupportedPlaces.add("taxi stand");
		mSupportedPlaces.add("train station");
		mSupportedPlaces.add("travel agency");
		mSupportedPlaces.add("university");
		mSupportedPlaces.add("veterinary care");
		mSupportedPlaces.add("zoo");
	}
}
