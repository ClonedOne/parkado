package com.yogaub.giorgio.parkado;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.yogaub.giorgio.parkado.utilties.Constants;
import com.yogaub.giorgio.parkado.utilties.Utils;

public class PermissionRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);
        int request = getIntent().getIntExtra(Constants.PERM_REQ, -1);
        switch (request){
            case Constants.LOCATION_PERMISSION:
                requestLocationPermission();
                break;
            case Constants.MODIFY_GPS_STATUS:
                modifyGPSStatus();
                break;
            default:
                Log.e(Constants.UNEXP_PAR, "PermissionRequestActivity received unexpected request value");
        }
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

    private void modifyGPSStatus(){
        Toast toast = Toast.makeText(this, getString(R.string.enable_gps), Toast.LENGTH_LONG);
        toast.show();
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

}
