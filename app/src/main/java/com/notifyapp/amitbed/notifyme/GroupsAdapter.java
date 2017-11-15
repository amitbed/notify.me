package com.notifyapp.amitbed.notifyme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.notifyapp.amitbed.nudges.R;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

/**
 * Created by amitbed on 18/10/2017.
 */

public class GroupsAdapter extends ArrayAdapter<String> {

    private final int mResource;
    private final Context mContext;
    private LocalDatabaseHandler mDbHandler;

    public GroupsAdapter(@NonNull Context context, @LayoutRes int resource, Vector<String> contactNames, LocalDatabaseHandler dbHandler) {
        super(context, resource, contactNames);
        mDbHandler = dbHandler;
        mResource = resource;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater groupsListViewInflater = LayoutInflater.from(mContext);
        String groupName = getItem(position);
        View itemView = null;
        if (groupName != null && !groupName.isEmpty()) {
            itemView = groupsListViewInflater.inflate(mResource, parent, false);
            TextView groupTxtView = itemView.findViewById(R.id.group_name_txt);
            groupTxtView.setText(groupName);
            ImageView groupMembers = itemView.findViewById(R.id.members_btn);
            final View finalItemView = itemView;
            groupMembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    membersBtnClick(finalItemView);
                }
            });
        }
        return itemView;
    }

    private void membersBtnClick(View view) {
        TextView groupNameTextView = view.findViewById(R.id.group_name_txt);
        String groupName = groupNameTextView.getText().toString();
        Map<String,String> contactsOfGroup = mDbHandler.readRegistrationTokensFromGroup(groupName);
        AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
        builder.setTitle("Group Members");
        StringBuilder membersStrBuilder = new StringBuilder();
        Collection<String> members = contactsOfGroup.values();
        for (String member : members){
            membersStrBuilder.append('\t');
            membersStrBuilder.append(member);
            membersStrBuilder.append('\n');
        }
        builder.setMessage(membersStrBuilder.toString());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                alertDialog.dismiss();
            }
        });
    }
}

