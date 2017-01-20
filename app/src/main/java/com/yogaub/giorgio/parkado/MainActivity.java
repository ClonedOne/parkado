package com.yogaub.giorgio.parkado;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yogaub.giorgio.parkado.services.FloatingViewService;
import com.yogaub.giorgio.parkado.utilties.Constants;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void startChatHead(View view) {
        Log.d(Constants.DBG_CHATHEAD, "User click on Start Chathead Button");

        if (checkDrawOverlayPermission()){
            startService(new Intent(this, FloatingViewService.class));
        } else {
            showMessageOKCancel(getString(R.string.perm_chathead), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, Constants.MODIFY_SYS_OVERLAY);
                }
            }, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast denied_t = Toast.makeText(MainActivity.this, getString(R.string.perm_chathead_denied), Toast.LENGTH_LONG);
                    denied_t.show();
                }
            });
        }

        /* Code to be reused elsewhere
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.DBG_CHATHEAD, "Permission is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                Log.d(Constants.DBG_CHATHEAD, "Permission was denied in the past, should show rationale");
                showMessageOKCancel(getString(R.string.perm_chathead), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, 123);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, 123);
            }
        }
        */

    }

    /* Code to be reused elsewhere
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.CHATHEAD_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(Constants.DBG_CHATHEAD, "User granted permission");
                    Intent intent = new Intent(this, FloatingViewService.class);
                    startService(intent);
                } else {
                    Log.d(Constants.DBG_CHATHEAD, "User denied permission");
                    Toast denied_t = Toast.makeText(this, getString(R.string.perm_chathead_denied), Toast.LENGTH_LONG);
                    denied_t.show();
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    */

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
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
