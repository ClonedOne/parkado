package com.yogaub.giorgio.parkado;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.yogaub.giorgio.parkado.fragments.ParkedFragment;
import com.yogaub.giorgio.parkado.fragments.SettingFragment;
import com.yogaub.giorgio.parkado.interfaces.OnFragmentInteractionListener;
import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener {

    private ArrayList<String> perms = new ArrayList<>();
    private FloatingActionButton fab;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFloatingView();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        askPermissions();
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        Class fragClass = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_parked:
                Log.d(Constants.DBG_UI, "Selected parked fragment");
                fragClass = ParkedFragment.class;
                break;
            case R.id.nav_free:
                break;
            case R.id.nav_manage:
                Log.d(Constants.DBG_UI, "Selected setting fragment");
                fragClass = SettingFragment.class;
                break;
            default:
                break;
        }

        try {
            if (fragClass != null) {
                fragment = (Fragment) fragClass.newInstance();
                fragmentManager.beginTransaction().add(R.id.home_activity_frame_layout, fragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void hideSettingsIntro(View view) {
        CardView introCard = (CardView) findViewById(R.id.settings_intro_card_view);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        if (introCard != null) {
            introCard.startAnimation(animationOut);
            introCard.setVisibility(View.GONE);
        } else
            Log.d(Constants.DBG_UI, "Settings intro card object is null");
    }

    /*
    Permission Management
     */

    private void askPermissions() {

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

        if (perms.size() > 0) {
            ActivityCompat.requestPermissions(HomeActivity.this, perms.toArray(new String[perms.size()]), Constants.MULTIPLE_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            String asked = perms.get(i);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.DBG_PERM, "User denied permission " + asked);
                switch (asked) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        Snackbar denied_l = Snackbar.make(fab, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
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


    private void checkOverlay() {
        if (checkDrawOverlayPermission()) {
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
