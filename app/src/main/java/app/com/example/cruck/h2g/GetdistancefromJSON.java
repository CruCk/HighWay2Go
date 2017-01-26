package app.com.example.cruck.h2g;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cruck on 26-01-2017.
 */

public class GetdistancefromJSON {

    //For debugging
    private static final String TAG = GetdistancefromJSON.class.getSimpleName();


    //Method to get distance from JSON data
    public String getDistance(String url) {
        String distanceInfo="N/A";
        HttpHandler sh = new HttpHandler();

        // Making a request to url and getting response
        Log.i(TAG, "Url is "+url);
        String jsonStr = sh.makeServiceCall(url);

        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                // Getting JSON Array node
                JSONArray contacts = jsonObj.getJSONArray("rows");
                JSONObject elements = contacts.getJSONObject(0);   //first object in rows...elements
                JSONArray element = elements.getJSONArray("elements");
                JSONObject distances = element.getJSONObject(0);
                JSONObject distance = distances.getJSONObject("distance");
                distanceInfo = distance.getString("text");
                Log.i(TAG, "distance is:  "+distanceInfo);

            } catch (final JSONException e) {
                Log.e(TAG, "JSON parsing failed. ");
            }
        } else {
            Log.e(TAG, "The JSON String is null. Din't even reach JSON parsing part.");

        }
        return distanceInfo;
    }
}