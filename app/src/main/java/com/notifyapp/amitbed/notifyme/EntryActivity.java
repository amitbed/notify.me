package com.notifyapp.amitbed.notifyme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.notifyapp.amitbed.nudges.R;

/**
 * Created by amitbed on 16/11/2017.
 */

public class EntryActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PermissionHandler.isRequireContactsPermission(this)) {
            PermissionHandler.requestContactsPermission(this);
        } else {
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
        }
    }
}
