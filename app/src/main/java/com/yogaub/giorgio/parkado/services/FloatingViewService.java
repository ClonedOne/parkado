package com.yogaub.giorgio.parkado.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.yogaub.giorgio.parkado.HomeActivity;
import com.yogaub.giorgio.parkado.PermissionRequestActivity;
import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.utilties.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by Giorgio on yogaub.
 *
 * This class is a service responsible for the creation and management of the Floating View.
 */

public class FloatingViewService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private WindowManager windowManager;
    private View floatingView;
    private View collapsedView;
    private View expandedView;
    private ImageView collapseButton;
    private ImageView parkedButton;
    private ImageView whereButton;
    private ImageView lookingForButton;
    private ImageView leavingButton;
    private ImageView cancelView;
    private WindowManager.LayoutParams floatingViewParams;
    private WindowManager.LayoutParams cancelParams;
    private int windowHeight;
    private int windowWidth;
    private int centerOfScreenByX;

    private GoogleApiClient mGoogleApiClient;
    private Geocoder geocoder;
    private LocationRequest mLocationRequest;

    private boolean fiveLocations = false;
    private Runnable task;
    private int locationArraySize = 15;
    private Location [] locationArray = new Location[locationArraySize];
    private int locationCounter;


    /*
    Lifecycle Management
     */

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialization
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        collapsedView = floatingView.findViewById(R.id.collapsed_container);
        expandedView = floatingView.findViewById(R.id.expanded_container);
        collapseButton = (ImageView) floatingView.findViewById(R.id.expanded_image_view);
        parkedButton = (ImageView) floatingView.findViewById(R.id.park_button);
        whereButton = (ImageView) floatingView.findViewById(R.id.where_button);
        lookingForButton = (ImageView) floatingView.findViewById(R.id.looking_for_button);
        leavingButton = (ImageView) floatingView.findViewById(R.id.leaving_button);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        floatingViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatingViewParams.gravity = Gravity.TOP | Gravity.START;
        floatingViewParams.x = 0;
        floatingViewParams.y = 100;
        windowManager.addView(floatingView, floatingViewParams);

        setButtonListeners();

        buildGoogleApiClient();
        geocoder = new Geocoder(this, Locale.getDefault());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    /*
    Interface Management
     */

    private boolean insideCancelArea() {
        return ((floatingViewParams.y > windowHeight - cancelView.getHeight() - floatingView.getHeight()) &&
                ((floatingViewParams.x > centerOfScreenByX - cancelView.getWidth() - floatingView.getWidth() / 2) &&
                        (floatingViewParams.x < centerOfScreenByX + cancelView.getWidth() / 2)));
    }

    private void addCancelBinView() {
        cancelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        cancelParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        cancelView = new ImageView(this);
        cancelView.setPadding(20, 20, 20, 50);
        cancelView.setImageResource(R.drawable.cancel);

        cancelParams.x = 0;
        cancelParams.y = 0;

        windowManager.addView(cancelView, cancelParams);
    }



    /*
    Button listeners
     */

    private void setButtonListeners(){
        setFloatingViewListener(floatingView, Constants.FLTNG_VW);
        setFloatingViewListener(collapseButton, Constants.CLLPS_BTN);
        setFloatingViewListener(parkedButton, Constants.PRKD_BTN);
        setFloatingViewListener(whereButton, Constants.WHR_BTN);
        setFloatingViewListener(lookingForButton, Constants.LKFR_BTN);
        setFloatingViewListener(leavingButton, Constants.LVNG_BTN);
    }

    private void setFloatingViewListener(View view, final int view_elem) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = floatingViewParams.x;
                        initialY = floatingViewParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        addCancelBinView();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        centerOfScreenByX = windowWidth / 2;

                        // If the movement is in a 10 by 10 box, it was a click.
                        if (Xdiff < 1 && Ydiff < 1) {
                            touchyFishy(view_elem);
                        } else {
                            // remove collapse view when it is in the cancel area
                            if (insideCancelArea()) {
                                stopSelf();
                            }
                        }
                        // always remove recycle bin ImageView when paper is dropped
                        windowManager.removeView(cancelView);
                        cancelView = null;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        floatingViewParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        floatingViewParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, floatingViewParams);
                        if (insideCancelArea()) {
                            cancelView.setImageResource(R.drawable.close);
                        } else {
                            cancelView.setImageResource(R.drawable.cancel);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void touchyFishy(int view_elem){
        switch (view_elem){
            case Constants.FLTNG_VW:
                expand();
                break;
            case Constants.PRKD_BTN:
                collapse();
                parked();
                break;
            case Constants.WHR_BTN:
                collapse();
                where();
                break;
            case Constants.LKFR_BTN:
                collapse();
                lookingFor();
                break;
            case Constants.LVNG_BTN:
                collapse();
                leaving();
                break;
            case Constants.CLLPS_BTN:
                collapse();
                break;
            default:
        }
    }



    /*
    Button Actions
     */

    private void expand() {
        Log.d(Constants.DBG_UI, "Clicked on expand button");
        collapsedView.setVisibility(View.GONE);
        expandedView.setVisibility(View.VISIBLE);
    }

    private void collapse() {
        Log.d(Constants.DBG_UI, "Clicked on collapse button");
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
    }

    private void parked() {
        Log.d(Constants.DBG_UI, "Clicked on parked button");
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        mGoogleApiClient.connect();
        Toast toast = Toast.makeText(this, getString(R.string.toast_saving_park), Toast.LENGTH_LONG);
        toast.show();
        get_location();
    }

    private void where() {
        Log.d(Constants.DBG_UI, "Clicked on where button");
        Intent intent = new Intent(FloatingViewService.this, HomeActivity.class);
        intent.putExtra("Action", Constants.fragParked);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void leaving() {
        Log.d(Constants.DBG_UI, "Clicked on leaving button");
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PARKED_LAT);
        editor.remove(Constants.PARKED_LONG);
        editor.apply();
        Toast toast = Toast.makeText(this, getString(R.string.toast_leaving), Toast.LENGTH_LONG);
        toast.show();
    }

    private void lookingFor() {
        Log.d(Constants.DBG_UI, "Clicked on where button");
        Intent intent = new Intent(FloatingViewService.this, HomeActivity.class);
        intent.putExtra("Action", Constants.fragLooking);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }



    /*
    Location related methods
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(Constants.DBG_LOC, "Connection established");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Constants.DBG_LOC, "Connection suspended");
        Snackbar snackbar = Snackbar.make(floatingView, getString(R.string.error_googleapi), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(Constants.DBG_LOC, "Connection Failed");
        Snackbar snackbar = Snackbar.make(floatingView, getString(R.string.error_googleapi), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void buildGoogleApiClient() {
        Log.d(Constants.DBG_LOC, "Building api client");
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void get_location() {
        Log.d(Constants.DBG_LOC, "Attempting to get location");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (ActivityCompat.checkSelfPermission(FloatingViewService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(Constants.DBG_LOC, "Requesting location permission again");
                            Intent intent = new Intent(FloatingViewService.this, PermissionRequestActivity.class);
                            intent.putExtra(Constants.PERM_REQ, Constants.LOCATION_PERMISSION);
                            startActivity(intent);
                            return;
                        }
                        repeatLocationRequests();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(Constants.DBG_LOC, "Need to change GPS settings");
                        Intent intent = new Intent(FloatingViewService.this, PermissionRequestActivity.class);
                        intent.putExtra(Constants.PERM_REQ, Constants.MODIFY_GPS_STATUS);
                        startActivity(intent);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(Constants.DBG_LOC, "Impossible to change GPS settings");
                        break;
                }
            }
        });

    }

    private void repeatLocationRequests(){
        final Handler handler = new Handler();
        task = new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.DBG_LOC, "Checking if location condition is met");
                if (!fiveLocations) {
                    Log.d(Constants.DBG_LOC, "Condition is not met");
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, FloatingViewService.this);
                    handler.postDelayed(task, 400);
                } else {
                    fiveLocations = false;
                }
            }
        };
        handler.postDelayed(task, 400);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            locationArray[locationCounter] = location;
            locationCounter++;
            if (locationCounter == locationArraySize){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                fiveLocations = true;
                locationCounter = 0;
                computeLocation();
            }
        }
    }

    private void computeLocation(){
        double finalLat = locationArray[locationArraySize - 1].getLatitude();
        double finalLong = locationArray[locationArraySize - 1].getLongitude();
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PARKED_LAT, Double.doubleToRawLongBits(finalLat));
        editor.putLong(Constants.PARKED_LONG, Double.doubleToRawLongBits(finalLong));
        editor.apply();

        try {
            List<Address> addresses = geocoder.getFromLocation(finalLat, finalLong, 1);
            String carLocationDecoded = "Your car is at: " + addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality();
            Log.d(Constants.DBG_LOC, carLocationDecoded);
            Log.d(Constants.DBG_LOC, "Final latitude and longitude: " + finalLat + " " + finalLong);
            sendSMS(carLocationDecoded);
        }
        catch (IOException e){
            Snackbar snackbar = Snackbar.make(floatingView, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }



    /*
    SMS related methods
     */

    public void sendSMS(String carLocationDecoded){
        Log.d(Constants.DBG_SMS, "Attempting to send SMS");
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        String number = sharedPreferences.getString(Constants.SMS_NUMBER, "");
        if (number.equals("")) {
            Log.d(Constants.DBG_SMS, "Could not find any saved number");
            return;
        } else{
            Log.d(Constants.DBG_SMS, "Number retrieved: " + number);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_SMS, "Requesting SMS permission again");
            Intent intent = new Intent(FloatingViewService.this, PermissionRequestActivity.class);
            intent.putExtra(Constants.PERM_REQ, Constants.SMS_PERMISSION);
            startActivity(intent);
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(number, null, carLocationDecoded, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}