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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button start_chathead_btn;
    private ArrayList<String> perms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_chathead_btn = (Button) findViewById(R.id.start_chathead_btn);
        askPermissions();
    }


    public void startChatHead(View view) {
        Log.d(Constants.DBG_CHATHEAD, "User click on Start Chathead Button");
        checkOverlay();
    }



    /*
    Permission Management
     */

    private void askPermissions(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_LOC, "Location permission is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                });
            } else
                perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_SMS, "SMS permission is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        perms.add(Manifest.permission.SEND_SMS);
                    }
                });
            } else
                perms.add(Manifest.permission.SEND_SMS);
        }

        if (perms.size() > 0){
            ActivityCompat.requestPermissions(MainActivity.this, perms.toArray(new String[perms.size()]), Constants.MULTIPLE_PERMISSION);
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
                        Snackbar denied_l= Snackbar.make(start_chathead_btn, getString(R.string.perm_location_denied), Snackbar.LENGTH_LONG);
                        denied_l.show();
                        break;
                    case Manifest.permission.SEND_SMS:
                        Snackbar denied_s = Snackbar.make(start_chathead_btn, getString(R.string.perm_sms_denied), Snackbar.LENGTH_LONG);
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




    /*
    Utility methods
     */



}
