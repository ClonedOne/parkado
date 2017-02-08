package com.yogaub.giorgio.parkado.utilties;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.yogaub.giorgio.parkado.HomeActivity;
import com.yogaub.giorgio.parkado.R;
import com.yogaub.giorgio.parkado.services.FloatingViewService;


/**
 * Created by Giorgio on yogaub.
 */

public class Utils {

    public static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .create()
                .show();
    }

    public static int getCurrCar (Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_PARKADO, Context.MODE_PRIVATE);
        int carType = sharedPreferences.getInt(Constants.CAR_TYPE, 0);
        Log.d(Constants.DBG_ALOG, "Retrieved car type: " + carType);
        if (carType == 0) {
            Log.d(Constants.DBG_ALOG, "Car type not found");
            Toast toast = Toast.makeText(context, context.getString(R.string.car_not_set), Toast.LENGTH_LONG);
            toast.show();
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra("Action", Constants.fragSettings);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            return 0;
        }
        else
            return carType;
    }

}
