package com.yogaub.giorgio.parkado;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yogaub.giorgio.parkado.utilties.Constants;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng carLocation = getCarLocation();
        mMap.addMarker(new MarkerOptions().position(carLocation).title(getString(R.string.map_marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 16));
    }


    private LatLng getCarLocation(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        double carLat = Double.longBitsToDouble(sharedPreferences.getLong(Constants.PARKED_LAT, 0));
        double carLong = Double.longBitsToDouble(sharedPreferences.getLong(Constants.PARKED_LONG, 0));
        Log.d(Constants.DBG_LOC, "Car is at: " + carLat + " " + carLong);
        return new LatLng(carLat, carLong);
    }
}
