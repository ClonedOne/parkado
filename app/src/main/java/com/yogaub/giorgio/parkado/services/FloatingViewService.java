package com.yogaub.giorgio.parkado.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

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
import com.yogaub.giorgio.parkado.PermissionRequestActivity;
import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.utilties.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by Giorgio on yogaub.
 *
 * This class is a service responsible for the creation and management of the Chat Head.
 */

public class FloatingViewService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private WindowManager windowManager;
    private View floatingView;
    private ImageView cancelView;
    private WindowManager.LayoutParams floatingViewParams;
    private WindowManager.LayoutParams cancelParams;
    private int windowHeight;
    private int windowWidth;
    private int centerOfScreenByX;

    private GoogleApiClient mGoogleApiClient;
    private Geocoder geocoder;
    private LocationRequest mLocationRequest;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*
    Interface generation
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialization
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //The root element of the collapsed view layout
        final View collapsedView = floatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = floatingView.findViewById(R.id.expanded_container);
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

        setFloatingViewListener(collapsedView, expandedView);

        windowManager.addView(floatingView, floatingViewParams);

        //Set the close button
        ImageView closeButton = (ImageView) floatingView.findViewById(R.id.expanded_image_view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });


        ImageView parkedButton = (ImageView) floatingView.findViewById(R.id.park_button);
        parkedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parked();
            }
        });

        buildGoogleApiClient();
        geocoder = new Geocoder(this, Locale.getDefault());

    }


    private void setFloatingViewListener(final View collapsedView, final View expandedView) {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // remove collapse view when it is in the cancel area
                            if (insideCancelArea(v)) {
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
                        if (insideCancelArea(v)) {
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

    private boolean isViewCollapsed() {
        return floatingView == null || floatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    private boolean insideCancelArea(View v) {
        return ((floatingViewParams.y > windowHeight - cancelView.getHeight() - v.getHeight()) &&
                ((floatingViewParams.x > centerOfScreenByX - cancelView.getWidth() - v.getWidth() / 2) &&
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
    private void parked() {
        Log.d(Constants.DBG_LOC, "Clicked on parked button");
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        mGoogleApiClient.connect();
        get_location();


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
                            Intent intent = new Intent(FloatingViewService.this, PermissionRequestActivity.class);
                            startActivity(intent);
                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, FloatingViewService.this);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(Constants.DBG_LOC, "Need to change settings");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(Constants.DBG_LOC, "Impossible to change settings");
                        break;

                }
            }
        });

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(Constants.DBG_LOC, String.valueOf(location.getLatitude()));
            Log.d(Constants.DBG_LOC, String.valueOf(location.getLongitude()));
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.d(Constants.DBG_LOC, "Your car is at: " + addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            }
            catch (IOException e){
                Snackbar snackbar = Snackbar.make(floatingView, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }
}
