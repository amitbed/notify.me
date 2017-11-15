package com.notifyapp.amitbed.notifyme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by amitbed on 27/09/2017.
 */

public class PermissionHandler {
    public static final int READ_CONTACTS = 0;
    public static final int PHONE_STATE = 1;

    public PermissionHandler() { }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestContactsPermission(Activity activity) {
        activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS); //request permission
    }

    /*@TargetApi(Build.VERSION_CODES.M)
    public static void requestPhonePermission(Activity activity) {
        activity.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE); //request permission
    }*/

    public static boolean isRequireContactsPermission(Activity activity){
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED);
    }

    /*public static boolean isRequirePhonePermission(Activity activity) {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED);
    }*/
}
