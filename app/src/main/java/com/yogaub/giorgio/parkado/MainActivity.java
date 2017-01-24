package com.yogaub.giorgio.parkado;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.Manifest;

import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

public class MainActivity extends AppCompatActivity {

    private Button start_chathead_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_chathead_btn = (Button) findViewById(R.id.start_chathead_btn);
        checkLocation();
    }


    public void startChatHead(View view) {
        Log.d(Constants.DBG_CHATHEAD, "User click on Start Chathead Button");
        checkOverlay();
    }



    /*
    Permission Management
     */

    private void checkLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_LOC, "Permission is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(Constants.DBG_LOC, "User granted permission");
                } else {
                    Log.d(Constants.DBG_LOC, "User denied permission");
                    Snackbar denied_s = Snackbar.make(start_chathead_btn, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                    denied_s.show();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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




    /*
    Utility methods
     */



}
