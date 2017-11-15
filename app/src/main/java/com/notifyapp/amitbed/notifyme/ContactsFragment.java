package com.notifyapp.amitbed.notifyme;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.notifyapp.amitbed.nudges.R;

import java.util.Iterator;

/**
 * Created by amitbed on 04/11/2017.
 */

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //********************************************************
    private static final int MIN_CONTACTS_FOR_A_GROUP = 2;
    private static final String[] PROJECTION =
            {
                    ContactsContract.CommonDataKinds.StructuredName._ID,
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER

            };
    private static final int CONTACT_NAME_INDEX = 1; //the index of the name column of the cursor table

    private static final String SELECTION = ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?";
    // Defines the array to hold the search criteria
    private String[] mSelectionArgs = { "" };
    private String mLookupKey;
    private ProgressBar contactProgressBar;
    //********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        IFirebaseDbHandler dbHandler = new FirebaseDbHandler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactProgressBar = root.findViewById(R.id.progressbar_contacts);
        contactProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.orderByChild("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                Iterator<DataSnapshot> usersIterator = children.iterator();
                if (usersIterator.hasNext()) {
                    User currentUser = usersIterator.next().getValue(User.class);
                    if (!currentUser.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mCurrentUserRegistrationToken = currentUser.getRegistrationToken();
                        addToListIfUserIsContact(currentUser.getPhoneNumber());
                    } else {
                        addNextUserToList();
                    }
                }else{
                    showListOfContacts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
