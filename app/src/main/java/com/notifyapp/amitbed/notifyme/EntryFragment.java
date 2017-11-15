package com.notifyapp.amitbed.notifyme;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.notifyapp.amitbed.nudges.R;

/**
 * Created by amitbed on 05/10/2017.
 */

public class EntryFragment extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_entry, container, false);
        if (PermissionHandler.isRequireContactsPermission(getActivity())) {
            PermissionHandler.requestContactsPermission(getActivity());
        } else {
            switchToContactsFragment();
        }
        return root;
    }

    private void switchToContactsFragment() {
        Fragment contacts = new ContactsFragment2();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, contacts).addToBackStack("ContactsFragment2").commit();
    }
}
