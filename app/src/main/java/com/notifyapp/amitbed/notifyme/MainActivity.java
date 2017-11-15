package com.notifyapp.amitbed.notifyme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.notifyapp.amitbed.nudges.BuildConfig;
import com.notifyapp.amitbed.nudges.R;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

/**
 * Created by amitbed on 20/09/2017.
 */

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mUsers;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    // Arbitrary request code value
    private static final int RC_SIGN_IN = 1;
    private boolean hasSignedInJustNow;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsers = firebaseDatabase.getReference().child("users");
        initializeAuthStateListener();
        if (PermissionHandler.isRequireContactsPermission(this)) {
            switchToEntryFragment();
        }else {
            switchToContactsFragment();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "signed in", Toast.LENGTH_SHORT).show();
                if (mCurrentUser == null) {
                    Log.d("xxx","user is null");
                    hasSignedInJustNow = true;
                }else {
                    Log.d("xxx"," user is not null - sending registration token to the server");
                    MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
                }
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        //get current activity on backstack
        int index = getFragmentManager().getBackStackEntryCount() - 1;
        FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
        String fragmentName = backEntry.getName();

        if (fragmentName.equals("ContactsFragment2")){
            finishAffinity();
        }else {
            super.onBackPressed();
        }
    }

    private void initializeAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                Log.d("xxx","on auth state changed - getting user");
                if (mCurrentUser != null) {
                    //user is signed in
                    if (hasSignedInJustNow) {
                        Log.d("xxx","just signed now - check if it comes AFTER onActivityResult");
                        registerUserToDbIfItIsANewUser();
                    }
                } else {
                    Log.d("xxx","user is null - starting registration");
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                                    .build(),RC_SIGN_IN);
                }
            }
        };
    }

    /**
     * Check if mCurrentUser exists on the database. If not, create a new user.
     */
    private void registerUserToDbIfItIsANewUser() {
        mUsers.orderByChild("userId").equalTo(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                //iterate over all of the users of the app - No problem here, because we're not on the ui thread
                for (DataSnapshot child : children){
                    User user = child.getValue(User.class);
                    if (user != null) {
                        if (user.getUserId().equals(mCurrentUser.getUid())){
                            // The user exists on db - update registration token is update on "onTokenRefresh"
                            MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
                            return;
                        }
                    }
                }
                mUsers.push().setValue(new User(mCurrentUser.getUid(), mCurrentUser.getPhoneNumber(), FirebaseInstanceId.getInstance().getToken()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean found = false;
        int i;
        switch(requestCode){
            case PermissionHandler.READ_CONTACTS:
                for (i=0; i < permissions.length; i++){
                    if (permissions[i].equals(Manifest.permission.READ_CONTACTS)){
                        found = true;
                        break;
                    }
                }
                if (found) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        switchToContactsFragment();
                    }else{
                        PermissionHandler.requestContactsPermission(this);
                    }
                }
                break;
            default:
        }
    }

    private void switchToEntryFragment() {
        Fragment contacts = new EntryFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, contacts).commit();
    }

    private void switchToContactsFragment() {
        Fragment contacts = new ContactsFragment2();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, contacts).addToBackStack("ContactsFragment2").commit();
    }

}
