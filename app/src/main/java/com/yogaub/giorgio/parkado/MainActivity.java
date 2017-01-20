package com.yogaub.giorgio.parkado;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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

    }

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
