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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yogaub.giorgio.parkado.fragments.LookingForFragment;
import com.yogaub.giorgio.parkado.fragments.ParkedFragment;
import com.yogaub.giorgio.parkado.fragments.SettingFragment;
import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    // Interface
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View headerLayout;
    private final String ACT_FRAG = "Active Fragment";

    // Permissions management
    private ArrayList<String> perms = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser user;

    private boolean drawerSet;


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

        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null)
            Log.d(Constants.DBG_AUTH, "mAuth == null");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(Constants.DBG_AUTH, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (!drawerSet) {
                        setDrawerElems();
                        drawerSet = true;
                    }
                } else {
                    Log.d(Constants.DBG_AUTH, "onAuthStateChanged:signed_out");
                    Intent out = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(out);
                    HomeActivity.this.finish();
                }

            }
        };

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = navigationView.getHeaderView(0);
        Intent intent = getIntent();
        switchFragmentOnIntent(intent);

        askPermissions();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchFragmentOnIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
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
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(ACT_FRAG);
            if (!(myFragment == null) && !(myFragment.getClass().equals(SettingFragment.class)))
                onNavigationItemSelected(navigationView.getMenu().getItem(Constants.fragSettings));
            else
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

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent exit = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(exit);
        finish();
    }

    public void setCar(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String selected = (String) spinner.getSelectedItem();
        Log.d(Constants.DBG_UI, "Selected Car Type item: " + selected);
        Resources r = getResources();
        String[] carTypes = r.getStringArray(R.array.car_type_array);
        int i;
        for (i = 0; i < carTypes.length; i++) {
            if (carTypes[i].equals(selected)) {
                break;
            }
        }
        if (i == 0){
            Log.d(Constants.DBG_UI, "Selected carType = 0");
            Snackbar snackbar = Snackbar.make(fab, getString(R.string.settings_car_type_error), Snackbar.LENGTH_LONG);
            snackbar.show();
        }else {
            Snackbar snackbar = Snackbar.make(fab, getString(R.string.settings_car_type_ok), Snackbar.LENGTH_LONG);
            snackbar.show();
            SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(Constants.CAR_TYPE, i);
            editor.apply();
        }
    }

    public void selectContact(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, Constants.PICK_CONTACT);
    }

    public void removeContact(View view) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.SMS_NUMBER);
        editor.apply();
        TextView contactName = (TextView) findViewById(R.id.settings_sms_contact_name);
        contactName.setText(getString(R.string.settings_sms_contact_text));
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
        switch (requestCode) {
            case Constants.MODIFY_SYS_OVERLAY:
                if (Settings.canDrawOverlays(this)) {
                    Log.d(Constants.DBG_UI, "Overlay Drawing allowed");
                    startService(new Intent(this, FloatingViewService.class));
                } else {
                    Log.d(Constants.DBG_UI, "Overlay Drawing NOT allowed");
                }
                break;
            case Constants.PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(Constants.DBG_CNTCS, "Contact picked");
                    Uri contactData = data.getData();
                    selectedContact(contactData);
                } else {
                    Log.e(Constants.DBG_CNTCS, "Problem in contact picking");
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
            case R.id.nav_manage:
                Log.d(Constants.DBG_UI, "Selected setting fragment");
                fragClass = SettingFragment.class;
                break;
            case R.id.nav_parked:
                Log.d(Constants.DBG_UI, "Selected parked fragment");
                fragClass = ParkedFragment.class;
                break;
            case R.id.nav_free:
                Log.d(Constants.DBG_UI, "Selected looking for fragment");
                fragClass = LookingForFragment.class;
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
                        R.anim.stay_anim,
                        R.anim.enter_anim,
                        R.anim.stay_anim
                );
                transaction.replace(R.id.home_activity_frame_layout, fragment, ACT_FRAG);
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
        int fragId = incomingIntent.getIntExtra("Action", Constants.fragSettings);
        Log.d(Constants.DBG_UI, "Starting main activity with fragId: " + fragId);
        onNavigationItemSelected(navigationView.getMenu().getItem(fragId));
    }

    private void setDrawerElems() {
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();
        Log.d(Constants.DBG_AUTH, name + " " + email + " " + photoUrl);
        TextView userName = (TextView) headerLayout.findViewById(R.id.userName);
        TextView userMail = (TextView) headerLayout.findViewById(R.id.userMail);
        CircleImageView userImage = (CircleImageView) headerLayout.findViewById(R.id.userImage);
        boolean un;

        if (name != null && !name.equals("")) {
            userName.setText(name);
            un = true;
        } else
            un = false;
        if (email != null && !email.equals("")) {
            userMail.setText(email);
            if (!un) {
                name = email.split("@")[0];
                userName.setText(name);
            }
        } else {
            userMail.setVisibility(View.INVISIBLE);
            if (!un)
                userName.setText(getString(R.string.parkado_user));
        }
        if (photoUrl != null)
            Glide.with(this).load(photoUrl).into(userImage);
    }



    /*
    Contact Management
     */

    private void selectedContact (Uri contactData) {
        if (contactData == null) {
            Log.e(Constants.DBG_CNTCS, "Problem in contact picking, contactData NULL");
            return;
        }
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(contactData, projection,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(numberIndex);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String contactName = cursor.getString(nameIndex);
            Log.d(Constants.DBG_CNTCS, "Contact picked: " + contactName + " " + phoneNumber);
            updateContactName(contactName);
            SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREF_PARKADO, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.SMS_NAME, contactName);
            editor.putString(Constants.SMS_NUMBER, phoneNumber);
            editor.apply();
        }
        if (cursor != null)
            cursor.close();
    }

    private void updateContactName(String contactName) {
        TextView contactNameTV = (TextView) findViewById(R.id.settings_sms_contact_name);
        contactNameTV.setText(contactName);
        Snackbar snackbar = Snackbar.make(fab, getString(R.string.settings_sms_contact_ok), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}
