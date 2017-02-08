package com.yogaub.giorgio.parkado.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.domain.Parking;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


public class LookingForFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private GoogleMap mMap;
    private View mapView;


    public LookingForFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_looking_for, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView = getActivity().findViewById(R.id.looking_for_map_frag);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.looking_for_map_frag);
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
            int carType = Utils.getCurrCar(getContext());
            if (carType == 0)
                return;
            getParkingSpots();
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


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.LOCATION_PERMISSION) {
            if (permissions.length == 1 && Objects.equals(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                int carType = Utils.getCurrCar(getContext());
                if (carType == 0)
                    return;
                getParkingSpots();
            } else {
                Snackbar denied_l = Snackbar.make(mapView, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                denied_l.show();
            }
        }
    }


    private void getParkingSpots() {
        try {
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "http://192.168.1.75:8000/service/parkings/";

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(Constants.DBG_ALOG, "Response is: " + response.toString());
                            populateMap(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Constants.DBG_ALOG, "Volley connection on getParkingSpots failed");
                }
            });
            queue.add(request);
        } catch (Exception e) {
            Log.e(Constants.DBG_ALOG, "Error in Volley on getParkingSpots");
            e.printStackTrace();
        }
    }

    private void populateMap(JSONArray response) {
        int carType = Utils.getCurrCar(getContext());
        Gson gson = new Gson();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateNow = new Date();
        if (carType == 0)
            return;
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Parking parking = gson.fromJson(obj.toString(), Parking.class);
                String createdDate = parking.getCreated().substring(0, parking.getCreated().length() - 8);
                Log.d(Constants.DBG_ALOG, "received parking date " + createdDate);
                Date parkDate = format.parse(createdDate);
                long difference = dateNow.getTime() - parkDate.getTime();
                long diffMins = (difference / (1000 * 60)) - 60;
                if (diffMins <= 10) {
                    setMarker(parking, diffMins);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.DBG_ALOG, "Error in handling of received JSON in populateMap");
            }
        }
    }

    private void setMarker(Parking parking, long diffMins) {
        LatLng loc = new LatLng(parking.getLastLat(), parking.getLastLong());
        float color = BitmapDescriptorFactory.HUE_YELLOW;
        if (diffMins < 5)
            color = BitmapDescriptorFactory.HUE_AZURE;
        mMap.addMarker(new MarkerOptions().position(loc).title(diffMins + " " + getString(R.string.map_marker_mins)).icon(BitmapDescriptorFactory.defaultMarker(color)));

    }

}
