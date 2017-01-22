package cz.uhk.fim.skoreto.todolist.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;

/**
 * Fragment umisteni a radiusu ukolu. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class TaskPlaceMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap gMap;
    MapView mapView;

    public static TaskPlaceMapFragment newInstance(Task task, DataModel dm) {
        TaskPlaceMapFragment f = new TaskPlaceMapFragment();
        Bundle args = new Bundle();
        boolean isTaskPlaceFilled = false;
        if (task.getTaskPlaceId() != -1) {
            // Pokud je vyplneno misto ukolu predej Lat Long
            isTaskPlaceFilled = true;
            TaskPlace taskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            args.putFloat("taskPlaceLat", (float) taskPlace.getLatitude());
            args.putFloat("taskPlaceLong", (float) taskPlace.getLongitude());
            args.putInt("taskPlaceRadius", taskPlace.getRadius());
        }
        args.putBoolean("isTaskPlaceFilled", isTaskPlaceFilled);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_map, container, false);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        // Inicializace mapView mapou
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    // Callback zavolany, kdyz je mapa pripravena
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (gMap != null) {
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            // Kontrola permission pro pristup k aktualni lokaci
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.setMyLocationEnabled(true);

            if (getArguments().getBoolean("isTaskPlaceFilled")) {
                // Pokud je vyplneno misto ukolu - vyznac jej na mape
                float taskPlaceLatitude = getArguments().getFloat("taskPlaceLat");
                float taskPlaceLongitude = getArguments().getFloat("taskPlaceLong");
                int taskPlaceRadius = getArguments().getInt("taskPlaceRadius");
                gMap.addMarker(new MarkerOptions().position(
                        new LatLng(taskPlaceLatitude, taskPlaceLongitude)));
                // Radius specifikovan v metrech by mel byt 0 nebo vetsi
                gMap.addCircle(new CircleOptions()
                        .center(new LatLng(taskPlaceLatitude, taskPlaceLongitude))
                        .radius(taskPlaceRadius)
                        .strokeColor(Color.RED).strokeWidth(7));

                // Nutne zavolat MapsInitializer pred volanim CameraUpdateFactory
                MapsInitializer.initialize(this.getActivity());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(taskPlaceLatitude, taskPlaceLongitude), 11);
                gMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
