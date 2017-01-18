package cz.uhk.fim.skoreto.todolist.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import cz.uhk.fim.skoreto.todolist.model.TaskPlace;

/**
 * Created by Tomas.
 */
public class GeoHelper {

    private TaskPlace currentPlace = null;
    private RequestQueue requestQueue;

    public GeoHelper(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    /**
     *
     * @param currentContext
     * @return
     */
    public TaskPlace getCurrentTaskPlace(final Context currentContext) {
        if (ActivityCompat.checkSelfPermission(currentContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(currentContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission odchyceny a handlovany pred volanim metody getCurrentTaskPlace
            return currentPlace;
        }

        LocationManager locationManager = (LocationManager)currentContext.getSystemService(Context.LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get current location
        final Location currentLocation = locationManager.getLastKnownLocation(provider);

        // Ziskani adresy soucasne pozice z coordinates
        // Oproti tride Geocoder vraci pristup s GeocodingAPI vzdy vysledek
//        requestQueue = Volley.newRequestQueue(currentContext);

        JsonObjectRequest request = new JsonObjectRequest("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                + "&key=AIzaSyC1Vaq8FOHelH58mXhZ3Zn8ksvPbsb9loo", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String currentPlaceAddress = response.getJSONArray("results")
                            .getJSONObject(0).getString("formatted_address");

                    // Poznamenej si, ze misto bylo zmeneno. Udrz si novou instanci
                    // pred pripadnym updatem databaze po potvrzeni editace ukolu.
                    currentPlace = new TaskPlace(currentLocation.getLatitude(),
                            currentLocation.getLongitude(), currentPlaceAddress, 100);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(currentContext, "Volley networking chyba",
                        Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
        return currentPlace;
    }

}
