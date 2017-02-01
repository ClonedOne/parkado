package com.yogaub.giorgio.parkado;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import java.util.ArrayList;

public class ActivityHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<String> perms = new ArrayList<>();
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFloatingView();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        askPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }



    /*
    Button Listeners
     */

    public void startFloatingView() {
        Log.d(Constants.DBG_CHATHEAD, "User click on Start Floating View Button");
        checkOverlay();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    /*
    Permission Management
     */

    private void askPermissions(){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_LOC, "Location permission is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                });
            } else
                perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_SMS, "SMS permission is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        perms.add(android.Manifest.permission.SEND_SMS);
                    }
                });
            } else
                perms.add(android.Manifest.permission.SEND_SMS);
        }

        if (perms.size() > 0){
            ActivityCompat.requestPermissions(ActivityHome.this, perms.toArray(new String[perms.size()]), Constants.MULTIPLE_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++){
            String asked = perms.get(i);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                Log.d(Constants.DBG_PERM, "User denied permission " + asked);
                switch (asked){
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        Snackbar denied_l= Snackbar.make(fab, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                        denied_l.show();
                        break;
                    case Manifest.permission.SEND_SMS:
                        Snackbar denied_s = Snackbar.make(fab, getString(R.string.perm_sms_denied), Snackbar.LENGTH_LONG);
                        denied_s.show();
                        break;
                    default:
                        Log.e(Constants.DBG_PERM, "Unexpected Permission Result Callback");
                        break;
                }
            } else {
                Log.d(Constants.DBG_PERM, "User granted permission " + asked);
            }
        }


    }


    private void checkOverlay(){
        if (checkDrawOverlayPermission()){
            Log.d(Constants.DBG_CHATHEAD, "Permission is already granted");
            startService(new Intent(this, FloatingViewService.class));
        } else {
            Log.d(Constants.DBG_CHATHEAD, "Permission is not granted");
            Utils.showMessageOKCancel(this, getString(R.string.perm_chathead), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, Constants.MODIFY_SYS_OVERLAY);
                }
            });
        }
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.MODIFY_SYS_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatingViewService.class));
            }
        }
    }

}
