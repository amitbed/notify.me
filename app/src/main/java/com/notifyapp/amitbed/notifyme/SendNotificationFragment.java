package com.notifyapp.amitbed.notifyme;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.notifyapp.amitbed.nudges.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by amitbed on 24/09/2017.
 */

public class SendNotificationFragment extends Fragment{
    //private ArrayList<String> mAddressees;
    private ArrayList<String> mRegistrationTokens;

    EditText titleTxt;
    EditText subTitleTxt;
    EditText messageTxt;
    Button sendNotificationBtn;
    View root;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int numOfContacts = getArguments().getInt("numOfContacts");
        if (numOfContacts > 0) {
            mRegistrationTokens = new ArrayList<>();
            for (int i=0; i < numOfContacts; i++){
                mRegistrationTokens.add(getArguments().getString("addressee_"+i));
            }
        }
        //getRegistrationTokens();
    }

    /*private String getOwnPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();

    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_send_notification,container,false);
        sendNotificationBtn = root.findViewById(R.id.send_notification_btn);
        progressBar = root.findViewById(R.id.progressbar);
        titleTxt = root.findViewById(R.id.title_txt);
        subTitleTxt = root.findViewById(R.id.sub_title_txt);
        messageTxt = root.findViewById(R.id.full_message_txt);
        sendNotificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check that the text fields of the actual notification are filled.
                if (titleTxt.getText().toString().equals("")){
                    Snackbar.make(root,getResources().getString(R.string.title_lbl) +" must be at least one character long",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (subTitleTxt.getText().toString().equals("")){
                    Snackbar.make(root, getResources().getString(R.string.sub_title_lbl) + " must be at least one character long",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                sendNotification();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRegistrationTokens.isEmpty()) {
            sendNotificationBtn.setEnabled(false);
            Toast.makeText(getActivity(), "Error: No contact has Nudges app installed", Toast.LENGTH_SHORT).show();
            Log.d("Errors","no registration tokens");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sendNotificationBtn.setTextColor(getActivity().getColor(android.R.color.white));
                sendNotificationBtn.setBackgroundColor(getActivity().getColor(android.R.color.darker_gray));
            }else {
                sendNotificationBtn.setTextColor(getResources().getColor(android.R.color.white));
                sendNotificationBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    private void sendNotification() {
        if (mRegistrationTokens.isEmpty()){
            Toast.makeText(getActivity(), "Problem occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        //creating JSON object
        JSONObject jsonObject = createJsonObject();
        if (jsonObject == null) {
            return;
        }
        //sending JSON object through REST api
        sendHttpsPostRequest(getActivity(), jsonObject);

    }

    @Nullable
    private JSONObject createJsonObject() {
        JSONArray registrationTokensArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            int i=0;
            for (String rt : mRegistrationTokens) {
                registrationTokensArray.put(i, rt);
            }
            String senderNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            if (senderNumber == null){
                senderNumber = "";
            }
            jsonObject.put("senderNumber",senderNumber);
            jsonObject.put("title", titleTxt.getText().toString());
            jsonObject.put("subTitle", titleTxt.getText().toString());
            jsonObject.put("message",messageTxt.getText().toString());
            jsonObject.put("registrationTokens",registrationTokensArray);
        } catch (JSONException e){
            Log.d("Errors","can't create notification json object");
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    private void sendHttpsPostRequest(Context context, JSONObject jsonObject) {
        showProgressDialog();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = "https://us-central1-nudge-s.cloudfunctions.net/sendNotificationToContacts";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,url,jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getActivity(), "Sent successfully", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Sent successfully", Toast.LENGTH_SHORT).show();
                hideProgressDialog();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); //"application/json; charset=utf-8"
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.INVISIBLE);
        titleTxt.setText("");
        subTitleTxt.setText("");
        messageTxt.setText("");
        titleTxt.requestFocus();
    }

    /*private void getRegistrationTokens() {
        mRegistrationTokens = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference users = db.getReference().child("users");
                final ArrayList<String> registrationTokens = new ArrayList<>();
                users.orderByChild("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children){  //iterate over all of the users of the app
                            User user = child.getValue(User.class);
                            if (user != null) {
                                if (mAddressees.contains(user.getPhoneNumber())) {
                                    registrationTokens.add(user.getRegistrationToken());
                                }
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!registrationTokens.isEmpty()) {
                                    mRegistrationTokens.addAll(registrationTokens);
                                    Toast.makeText(getActivity(), "Ready to send notification", Toast.LENGTH_SHORT).show();
                                    sendNotificationBtn.setEnabled(true);
                                } else{
                                    Toast.makeText(getActivity(), "Error: No contact has Nudges app installed", Toast.LENGTH_SHORT).show();
                                    Log.d("Errors","no registration tokens");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        sendNotificationBtn.setTextColor(getActivity().getColor(android.R.color.white));
                                        sendNotificationBtn.setBackgroundColor(getActivity().getColor(android.R.color.darker_gray));
                                    }else {
                                        sendNotificationBtn.setTextColor(getResources().getColor(android.R.color.white));
                                        sendNotificationBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).start();
    }*/
}
