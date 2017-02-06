package com.yogaub.giorgio.parkado.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import java.util.Objects;


public class ParkedFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private GoogleMap mMap;
    private View mapView;


    public ParkedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parked, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView = getActivity().findViewById(R.id.parked_map_frag);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.parked_map_frag);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(Constants.DBG_LOC, "On Map Ready Callback.");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_LOC, "Location permission is available. Shows MyLocation button.");
            mMap.setMyLocationEnabled(true);
        } else {
            Log.d(Constants.DBG_LOC, "Location permission is not available. Asking for permission.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Utils.showMessageOKCancel(getContext(), getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
            }
        }
        LatLng carLocation = getCarLocation();
        if (carLocation != null) {
            mMap.addMarker(new MarkerOptions().position(carLocation).title(getString(R.string.map_marker)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 16));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.LOCATION_PERMISSION) {
            if (permissions.length == 1 && Objects.equals(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                Snackbar denied_l = Snackbar.make(mapView, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                denied_l.show();
            }
        }
    }

    private LatLng getCarLocation() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREF_PARKADO, Context.MODE_PRIVATE);
        double carLat = Double.longBitsToDouble(sharedPreferences.getLong(Constants.PARKED_LAT, 0));
        double carLong = Double.longBitsToDouble(sharedPreferences.getLong(Constants.PARKED_LONG, 0));
        if (carLat == carLong && carLat == 0)
            return null;
        Log.d(Constants.DBG_LOC, "Car is at: " + carLat + " " + carLong);
        return new LatLng(carLat, carLong);
    }
}
