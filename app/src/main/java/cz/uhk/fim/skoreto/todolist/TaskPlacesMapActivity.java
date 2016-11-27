package cz.uhk.fim.skoreto.todolist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;

public class TaskPlacesMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private DataModel dataModel;
    private int listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dataModel = new DataModel(this);

        Intent anyIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyIntent.getIntExtra("listId", 1);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        List<Task> listTasksByListId = dataModel.getTasksByListId(listId, true);
        boolean firstTaskPlaceFound = false;
        // Zobraz ukoly, ktere maji zadane misto na mape
        for (Task currentTask : listTasksByListId) {
            if (currentTask.getTaskPlaceId() != -1) {
                TaskPlace currentTaskPlace = dataModel.getTaskPlace(currentTask.getTaskPlaceId());
                LatLng currentLatLng = new LatLng(currentTaskPlace.getLatitude(),
                        currentTaskPlace.getLongitude());
                gMap.addMarker(new MarkerOptions().position(currentLatLng).title(currentTask.getName()));

                // Presun kameru nad prvni nalezeny ukol (s nejblizsim datumem splneni)
                if (!firstTaskPlaceFound) {
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    firstTaskPlaceFound = true;
                }
            }
        }

        // Kontrola permission k GPS
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Povoleni tlacitka GPS na aktualni lokalizaci
        gMap.setMyLocationEnabled(true);
    }
}
