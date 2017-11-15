package com.notifyapp.amitbed.notifyme;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.notifyapp.amitbed.nudges.R;

/**
 * Created by amitbed on 12/10/2017.
 */

public class ShowMessageActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView titleTxt;
    TextView subTitleTxt;
    TextView messageTxt;
    TextView senderTxt;

    // Defines a constant that identifies the loader
    private static final int DETAILS_QUERY_ID = 1;
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
    private String senderPhoneNumber;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        setFinishOnTouchOutside(false);
        titleTxt = findViewById(R.id.title_shown_txt);
        subTitleTxt = findViewById(R.id.sub_title_shown_txt);
        messageTxt = findViewById(R.id.message_shown_txt);
        senderTxt = findViewById(R.id.senders_name_shown_txt);
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            titleTxt.setText(extras.getString("noti_title"));
            subTitleTxt.setText(extras.getString("noti_sub_title"));
            messageTxt.setText(extras.getString("noti_message"));
            senderTxt.setText("");
            mLookupKey = extras.getString("noti_sender");
            senderPhoneNumber = extras.getString("noti_sender");
        }else {
            Toast.makeText(getApplicationContext(), "Problem displaying the message", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().initLoader(DETAILS_QUERY_ID, null, this);
    }

    public void gotItButtonClick(View view) {
        finishAffinity();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Choose the proper action
        switch (id) {
            case DETAILS_QUERY_ID:
                // Assigns the selection parameter
                mSelectionArgs[0] = mLookupKey;
                // Starts the query
                return new CursorLoader(
                    this,
                    ContactsContract.Data.CONTENT_URI,
                    PROJECTION,
                    SELECTION,
                    mSelectionArgs,
                    null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            String name = data.getString(CONTACT_NAME_INDEX);
            if (!name.isEmpty()) {
                senderTxt.setText(name);
            } else {
                senderTxt.setText(senderPhoneNumber);
            }
        }else {
            senderTxt.setText(senderPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
