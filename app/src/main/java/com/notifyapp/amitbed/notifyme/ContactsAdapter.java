package com.notifyapp.amitbed.notifyme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.notifyapp.amitbed.nudges.R;

import java.util.Vector;

/**
 * Created by amitbed on 15/10/2017.
 */

public class ContactsAdapter extends ArrayAdapter<String> {

    private final Vector<String> mSelectedNames;
    private int mResource;
    private Context mContext;

    public ContactsAdapter(Context context, int resource, Vector<String> contactNames, Vector<String> selectedNames){
        super(context, resource, contactNames);
        mResource = resource;
        mContext = context;
        mSelectedNames = selectedNames;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater contactListViewInflater = LayoutInflater.from(mContext);
        String contactName = getItem(position);
        View itemView = null;
        if (contactName != null && !contactName.isEmpty()) {
            itemView = contactListViewInflater.inflate(mResource, parent, false);
            TextView contactTxtView = itemView.findViewById(R.id.contact_name_txt);
            CheckBox contactCheckBox = itemView.findViewById(R.id.contact_checkbox);
            contactTxtView.setText(contactName);
            if (mSelectedNames.contains(contactName)){
                contactTxtView.setTextColor(mContext.getResources().getColor(R.color.textCheckedColor));
                contactCheckBox.setChecked(true);
            }
        }
        return itemView;
        /*String contactName = getItem(position);
        if (contactName != null && !contactName.isEmpty()) {
            ViewHolder holder;
            LayoutInflater contactListViewInflater = LayoutInflater.from(mContext);
            if (convertView == null) {
                convertView = contactListViewInflater.inflate(mResource, parent, false);
                holder = new ViewHolder();
                holder.contactTxtView = convertView.findViewById(R.id.contact_name_txt);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.contactTxtView.setText(contactName);
        }
        return convertView;*/
    }

    private static class ViewHolder {
        private TextView contactTxtView;
    }
}
