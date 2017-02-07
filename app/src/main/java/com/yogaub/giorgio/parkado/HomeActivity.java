package com.yogaub.giorgio.parkado;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.yogaub.giorgio.parkado.fragments.HomeFragment;
import com.yogaub.giorgio.parkado.fragments.LookingForFragment;
import com.yogaub.giorgio.parkado.fragments.ParkedFragment;
import com.yogaub.giorgio.parkado.fragments.SettingFragment;
import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    // Interface
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private DrawerLayout drawer;

    // Permissions management
    private ArrayList<String> perms = new ArrayList<>();



    /*
    Lifecycle Management
     */

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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Intent intent = getIntent();
        switchFragmentOnIntent(intent);

        askPermissions();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchFragmentOnIntent(intent);
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

    public void hideSettingsIntro(View view) {
        CardView introCard = (CardView) findViewById(R.id.settings_intro_card_view);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        if (introCard != null) {
            introCard.startAnimation(animationOut);
            introCard.setVisibility(View.GONE);
        } else
            Log.d(Constants.DBG_UI, "Settings intro card object is null");
    }

    public void hideHomeIntro(View view) {
        CardView introCard = (CardView) findViewById(R.id.home_intro_card_view);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        if (introCard != null) {
            introCard.startAnimation(animationOut);
            introCard.setVisibility(View.GONE);
        } else
            Log.d(Constants.DBG_UI, "Home intro card object is null");
    }

    public void selectContact(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, Constants.PICK_CONTACT);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent exit = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(exit);
    }

    public void setCar(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String selected = (String) spinner.getSelectedItem();
        Log.d(Constants.DBG_UI, "Selected item: " + selected);
        Resources r = getResources();
        String[] carTypes = r.getStringArray(R.array.car_type_array);
        int i;
        for (i = 0; i < carTypes.length; i++){
            if (carTypes[i].equals(selected)) {
                break;
            }
        }
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.CAR_TYPE, i+1);
        editor.apply();
    }

    public void removeContact(View view) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.SMS_NUMBER);
        editor.apply();
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
        switch (requestCode){
            case Constants.MODIFY_SYS_OVERLAY:
                if (Settings.canDrawOverlays(this)) {
                    Log.d(Constants.DBG_UI, "Overlay Drawing allowed");
                    startService(new Intent(this, FloatingViewService.class));
                }else{
                    Log.d(Constants.DBG_UI, "Overlay Drawing NOT allowed");
                }
                break;
            case Constants.PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(Constants.DBG_CNTCS, "Contact picked");
                    Uri contactData = data.getData();
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor cursor = getContentResolver().query(contactData, projection,
                            null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String phoneNumber = cursor.getString(numberIndex);
                        Log.d(Constants.DBG_CNTCS, "Number picked: " + phoneNumber);
                        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.SMS_NUMBER, phoneNumber);
                        editor.apply();
                    }
                    if (cursor != null)
                        cursor.close();
                }else {
                    Log.d(Constants.DBG_CNTCS, "Problem in contact picking");
                }
                break;
        }
    }



    /*
    Fragment Management
     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked())
            return false;
        Log.v(Constants.DBG_UI, "Performing fragment switch with menu item: " + item.toString());
        int id = item.getItemId();
        Fragment fragment;
        Class fragClass = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (id) {
            case R.id.nav_home:
                Log.d(Constants.DBG_UI, "Selected home fragment");
                fragClass = HomeFragment.class;
                break;
            case R.id.nav_parked:
                Log.d(Constants.DBG_UI, "Selected parked fragment");
                fragClass = ParkedFragment.class;
                break;
            case R.id.nav_free:
                Log.d(Constants.DBG_UI, "Selected looking for fragment");
                fragClass = LookingForFragment.class;
                break;
            case R.id.nav_manage:
                Log.d(Constants.DBG_UI, "Selected setting fragment");
                fragClass = SettingFragment.class;
                break;
            case R.id.nav_log_out:
                drawer.closeDrawer(GravityCompat.START);
                logOut();
                return true;
            default:
                break;
        }

        try {
            if (fragClass != null) {
                fragment = (Fragment) fragClass.newInstance();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_anim,
                        R.anim.stay_anim
                );
                transaction.replace(R.id.home_activity_frame_layout, fragment);
                transaction.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        item.setChecked(true);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragmentOnIntent(Intent incomingIntent) {
        int fragId = incomingIntent.getIntExtra("Action", Constants.fragHome);
        Log.d(Constants.DBG_UI, "Starting main activity with fragId: " + fragId);
        onNavigationItemSelected(navigationView.getMenu().getItem(fragId));
    }


}
