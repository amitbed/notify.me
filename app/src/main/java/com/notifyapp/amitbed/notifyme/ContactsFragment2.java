package com.notifyapp.amitbed.notifyme;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.notifyapp.amitbed.nudges.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Created by amitbed on 14/10/2017.
 */

public class ContactsFragment2 extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MIN_CONTACTS_FOR_A_GROUP = 2;
    private Vector<String> contactListNames;
    private Vector<String> contactListRegistrationTokens;
    private Vector<String> selectedContactsRegistrationTokens;
    private Vector<View> selectedContactsViews;
    private Vector<View> selectedContactsNames;

    private ProgressBar contactProgressBar;

    // Defines a constant that identifies the loader
    //private static final int queryId = 2;
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
    private Iterable<DataSnapshot> children;
    private Iterator<DataSnapshot> usersIterator;
    private String mCurrentUserRegistrationToken;
    private Button selectContactsBtn;

    private int queryId = 0;
    private String mGroupName;
    private Button addNewGroupBtn;
    private ContactsAdapter contactListAdapter;
    private TextView groupsDeviderTxtView;
    private ListView groupsListView;
    private Vector<String> groupsList; // holding group names
    private GroupsAdapter groupsAdapter;
    private LocalDatabaseHandler mDbHandler;
    private Map<String,String> contactsToAddToGroup; // contains map of <registrationToken, name>


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getGroupsFromLocalDB();
    }

    private void addNextUserToList() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);
        mCurrentUserRegistrationToken = null;
        contactListNames = new Vector<>();

        contactListRegistrationTokens = new Vector<>();
        contactProgressBar = root.findViewById(R.id.progressbar_contacts);
        contactProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.orderByChild("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                children = dataSnapshot.getChildren();
                usersIterator = children.iterator();
                addNextUserToList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        selectContactsBtn = root.findViewById(R.id.select_contacts_btn);
        addNewGroupBtn = root.findViewById(R.id.add_new_group_btn);
        groupsDeviderTxtView = root.findViewById(R.id.groups_divider_txt);
        groupsListView = root.findViewById(R.id.groups_list);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDbHandler != null) {
            mDbHandler.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!groupsList.isEmpty()){
            groupsDeviderTxtView.setVisibility(View.VISIBLE);
            groupsListView.setVisibility(View.VISIBLE);
            if (groupsAdapter == null) {
                groupsAdapter = new GroupsAdapter(getActivity(), R.layout.group_list_item, groupsList, mDbHandler);
                groupsListView.setAdapter(groupsAdapter);
                setGroupItemClick();
            }else {
                groupsAdapter.notifyDataSetChanged();
            }
        }
        selectContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNewPush();
            }
        });
        selectContactsBtn.setClickable(false);
        addNewGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactsToAddToGroup.size() < MIN_CONTACTS_FOR_A_GROUP){
                    Toast.makeText(getActivity(), R.string.minimum_contacts_txt,Toast.LENGTH_SHORT).show();
                } else {
                    mDbHandler.addContactToGroup(mGroupName, contactsToAddToGroup);
                    groupsList.add(0,mGroupName);
                    //save registration tokens on local db
                    //add to list of groups
                    groupsDeviderTxtView.setVisibility(View.VISIBLE);
                    groupsListView.setVisibility(View.VISIBLE);
                    if (groupsAdapter == null) {
                        groupsAdapter = new GroupsAdapter(getActivity(), R.layout.group_list_item, groupsList, mDbHandler);
                        groupsListView.setAdapter(groupsAdapter);
                        setGroupItemClick();
                    }else {
                        groupsAdapter.notifyDataSetChanged();
                    }
                    addNewGroupBtn.setVisibility(View.GONE);
                    addNewGroupBtn.setClickable(false);
                    mGroupName = null;
                    selectedContactsRegistrationTokens.clear();
                    selectedContactsViews.clear();
                }
            }
        });
        addNewGroupBtn.setClickable(false);
    }

    private void setGroupItemClick() {
        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView groupNameTextView = view.findViewById(R.id.group_name_txt);
                String groupName = groupNameTextView.getText().toString();
                Map<String,String> contactsOfGroup = mDbHandler.readRegistrationTokensFromGroup(groupName);
                if (!addNewGroupBtn.isClickable()) { // user wants to pick contacts for sending notification
                    //get Registration tokens of the groups / groups
                    //add them to "selectedContactsRegistrationTokens"
                    if (setItemAsChecked(view, false)) {
                        selectedContactsRegistrationTokens.addAll(contactsOfGroup.keySet());
                    } else {
                        selectedContactsRegistrationTokens.removeAll(contactsOfGroup.keySet());
                        uncheckCheckedContactsFromTheSameGroup(contactsOfGroup.values());
                    }
                    showOrHideSelectContactsButtonOnItemClick();
                } else {
                    if (setItemAsChecked(view, false)) {
                        contactsToAddToGroup.putAll(contactsOfGroup);
                    } else {
                        for (String rtOfContact : contactsOfGroup.keySet()){
                            contactsToAddToGroup.remove(rtOfContact);
                        }
                    }
                }
            }
        });
    }

    private void uncheckCheckedContactsFromTheSameGroup(Collection<String> namesOfGroup) {
        if (selectedContactsViews.isEmpty()) {
            return;
        }
        for (String contactName : namesOfGroup){
            for (int i=0; i< selectedContactsViews.size(); i++){
                TextView tv = selectedContactsViews.elementAt(i).findViewById(R.id.contact_name_txt);
                if (tv.getText().toString().equals(contactName)){
                    View view = selectedContactsViews.elementAt(i);
                    CheckBox checkbox = view.findViewById(R.id.contact_checkbox);
                    if (checkbox.isChecked()){
                        TextView name = view.findViewById(R.id.contact_name_txt);
                        checkbox.setChecked(false);
                        name.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.create_a_group:
                createGroup();
                break;
            case R.id.send_feedback:
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse ("market://details?id=APP ID"));
                startActivity(intent);*/
                break;
        }
        return false;
    }

    private void createGroup() {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle("New Group");
        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //overriden
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //overriden
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String groupName = input.getText().toString().trim();
                if (groupName.isEmpty()) {
                    Toast.makeText(getActivity(),"Group name should not be empty",Toast.LENGTH_SHORT).show();
                } if (groupsList.contains(groupName)){
                    Toast.makeText(getActivity(),"This Group name is already being used",Toast.LENGTH_SHORT).show();
                } else {
                    mGroupName = groupName;
                    contactsToAddToGroup = new HashMap<>();
                    addNewGroupBtn.setVisibility(View.VISIBLE);
                    addNewGroupBtn.setClickable(true);
                    cancelContactsSelectionForContacts();
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void cancelContactsSelectionForContacts() {
        contactListNames.clear();
        contactListRegistrationTokens.clear();
        selectContactsBtn.setClickable(false);
        selectContactsBtn.setVisibility(View.GONE);

    }

    private void sendNewPush() {
        Fragment sendNotificationFragment = new SendNotificationFragment();
        Bundle addressees = new Bundle();
        int numOfContacts =0;
        for (int i=0; i < selectedContactsRegistrationTokens.size(); i++){
            numOfContacts++;
            addressees.putString("addressee_"+i,selectedContactsRegistrationTokens.elementAt(i));
        }
        addressees.putInt("numOfContacts",numOfContacts);
        sendNotificationFragment.setArguments(addressees);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, sendNotificationFragment).addToBackStack("SendNotificationFragment").commit();
    }

    private void addToListIfUserIsContact(String userPhoneNumber) {
        mLookupKey = userPhoneNumber;
        getLoaderManager().initLoader(queryId, null, this);
        queryId++;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Assigns the selection parameter
        mSelectionArgs[0] = mLookupKey;
        // Starts the query
        return new CursorLoader(
                getActivity(),
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCurrentUserRegistrationToken == null){
            Log.d("Errors","registration token is null");
            return;
        }
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            String userName = data.getString(CONTACT_NAME_INDEX);
            if (!userName.isEmpty()) {
                contactListRegistrationTokens.add(mCurrentUserRegistrationToken);
                contactListNames.add(userName);
            }
            mCurrentUserRegistrationToken = null;
        }
        addNextUserToList();
    }

    private void showListOfContacts() {
        selectedContactsRegistrationTokens = new Vector<>();
        selectedContactsViews = new Vector<>();
        selectedContactsNames = new Vector<>(); // TODO: change selected contacts to be an object: name, rt, view
        if (contactProgressBar != null){
            contactProgressBar.setVisibility(View.INVISIBLE);
        }
        if (contactListNames.isEmpty()){
            // Show "No Contacts Available" text
            getActivity().findViewById(R.id.no_contacts_txt).setVisibility(View.VISIBLE);
        }else{
            Collections.sort(contactListNames, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareToIgnoreCase(t1);
                }
            });
            //initiate listview
            contactListAdapter = new ContactsAdapter(getActivity(),R.layout.contact_list_item, contactListNames, selectedContactsNames);
            ListView contactListView = getActivity().findViewById(R.id.contacts_list);
            contactListView.setAdapter(contactListAdapter);
            setContactItemClick(contactListView);
        }
    }

    private void setContactItemClick(ListView contactListView) {
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String contactRegistrationToken = contactListRegistrationTokens.elementAt(i);
                boolean isUserWantToCheckContact = setItemAsChecked(view, true);
                if (!addNewGroupBtn.isClickable()) { // user wants to pick contacts for sending notification
                    if (isUserWantToCheckContact){
                        selectedContactsRegistrationTokens.add(contactRegistrationToken);
                        selectedContactsViews.add(view);
                    }else{
                        selectedContactsRegistrationTokens.remove(contactRegistrationToken);
                        selectedContactsViews.add(view);
                    }
                    showOrHideSelectContactsButtonOnItemClick();
                }else{ //user wants to pick contacts for creating a group
                    if (mGroupName != null && !mGroupName.equals("")) {
                        if (isUserWantToCheckContact){ // contact was checked before and now user wants to uncheck
                            TextView contactNameTxtView = view.findViewById(R.id.contact_name_txt);
                            String contactName = contactNameTxtView.getText().toString();
                            contactsToAddToGroup.put(contactListRegistrationTokens.elementAt(i), contactName);
                        }else{
                            contactsToAddToGroup.remove(contactListRegistrationTokens.elementAt(i));
                        }
                    }
                }
            }
        });
    }

    private void showOrHideSelectContactsButtonOnItemClick() {
        if (!selectedContactsRegistrationTokens.isEmpty()) {
            if (!selectContactsBtn.isClickable()) {
                selectContactsBtn.setVisibility(View.VISIBLE);
                selectContactsBtn.setClickable(true);
            }
        } else {
            selectContactsBtn.setVisibility(View.GONE);
            selectContactsBtn.setClickable(false);
        }
    }

    /**
     *
     * @param view
     * @param isContact - if true - then the view is a contact item, else, the view is a group item
     * @return true, if the user wants to check the contact.
     *         false, if the user wants to uncheck the contact
     */
    private boolean setItemAsChecked(View view, boolean isContact) {
        CheckBox checkbox;
        TextView name;
        if (isContact) {
            //TextView contactNumber = view.findViewById(R.id.contact_phone_number_txt);
            checkbox = view.findViewById(R.id.contact_checkbox);
            name = view.findViewById(R.id.contact_name_txt);
        }else{
            checkbox = view.findViewById(R.id.group_checkbox);
            name = view.findViewById(R.id.group_name_txt);
        }

        if (checkbox.isChecked()){
            checkbox.setChecked(false);
            name.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            return false;
        }
        checkbox.setChecked(true);
        name.setTextColor(getResources().getColor(R.color.textCheckedColor));
        return true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("Errors", "On Loader Reset");
    }

    public void getGroupsFromLocalDB() {
        mDbHandler = new LocalDatabaseHandler(getActivity());
        groupsList = mDbHandler.readGroupNames();
    }
}
