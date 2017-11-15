package com.notifyapp.amitbed.notifyme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.notifyapp.amitbed.notifyme.LocalDatabaseContract.GroupsEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by amitbed on 16/10/2017.
 */

public class LocalDatabaseHandler extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Groups.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + GroupsEntry.TABLE_NAME + " (" +
                    GroupsEntry._ID + " INTEGER PRIMARY KEY," +
                    GroupsEntry.COLUMN_NAME_GROUP_NAME + " TEXT," +
                    GroupsEntry.COLUMN_NAME_USER_RT + " TEXT," +
                    GroupsEntry.COLUMN_NAME_USER_NAME + " TEXT)";

    //--for querying
    // Define a projection that specifies which columns from the database
// you will actually use after this query.
    private String[] projection = {
            GroupsEntry.COLUMN_NAME_USER_RT,
            GroupsEntry.COLUMN_NAME_USER_NAME
    };

    // Filter results WHERE "title" = 'My Title'
    private String selection = GroupsEntry.COLUMN_NAME_GROUP_NAME + " = ?";
    private String[] selectionArgs = { "" };


    private String[] projection_group_names = {
            GroupsEntry.COLUMN_NAME_GROUP_NAME
    };

    public LocalDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addContactToGroup(String groupName, String userRt, String name) {

        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase(); //TODO: put in a background thread

        ContentValues values = new ContentValues();

        // Create a new map of values, where column names are the keys
        values.put(GroupsEntry.COLUMN_NAME_GROUP_NAME,groupName);
        values.put(GroupsEntry.COLUMN_NAME_USER_RT, userRt);
        values.put(GroupsEntry.COLUMN_NAME_USER_NAME, name);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(GroupsEntry.TABLE_NAME, null, values);
    }

    public void addContactToGroup(String groupName, Map<String,String> contacts) {

        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase(); //TODO: put in a background thread

        for (Map.Entry<String,String> contact : contacts.entrySet()) {
            ContentValues values = new ContentValues();

            // Create a new map of values, where column names are the keys
            values.put(GroupsEntry.COLUMN_NAME_GROUP_NAME, groupName);
            values.put(GroupsEntry.COLUMN_NAME_USER_RT, contact.getKey());
            values.put(GroupsEntry.COLUMN_NAME_USER_NAME, contact.getValue());
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(GroupsEntry.TABLE_NAME, null, values);
        }
    }

    public Map<String,String> readRegistrationTokensFromGroup(String groupName){

        // Gets the data repository in read mode
        SQLiteDatabase db = getReadableDatabase(); //TODO: put in a background thread

        selectionArgs[0] = groupName;
        Cursor cursor = db.query(
                GroupsEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        Map<String,String> result = new HashMap<>();
        while(cursor.moveToNext()) {
            result.put(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupsEntry.COLUMN_NAME_USER_RT)),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupsEntry.COLUMN_NAME_USER_NAME)
                    ));
        }
        cursor.close();
        return result;
    }

    public Vector<String> readGroupNames(){

        // Gets the data repository in read mode
        SQLiteDatabase db = getReadableDatabase(); //TODO: put in a background thread

        Cursor cursor = db.query(
                GroupsEntry.TABLE_NAME,                     // The table to query
                projection_group_names,                     // The columns to return
                null,                                       // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                GroupsEntry.COLUMN_NAME_GROUP_NAME,         // group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );
        Vector<String> result = new Vector<>();
        while(cursor.moveToNext()) {
            result.add(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupsEntry.COLUMN_NAME_GROUP_NAME)));
        }
        cursor.close();
        return result;
    }
}
