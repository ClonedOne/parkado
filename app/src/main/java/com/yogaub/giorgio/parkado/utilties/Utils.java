package com.yogaub.giorgio.parkado.utilties;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


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

}
