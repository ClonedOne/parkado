package com.yogaub.giorgio.parkado;

import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

public class PermissionRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);
        requestLocationPermission();
    }

    private void requestLocationPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            Utils.showMessageOKCancel(this, getString(R.string.perm_location), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(PermissionRequestActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
                }
            });
        } else {
            ActivityCompat.requestPermissions(PermissionRequestActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
        }
    }
}
