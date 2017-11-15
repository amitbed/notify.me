package com.notifyapp.amitbed.notifyme;

import android.provider.BaseColumns;

/**
 * Created by amitbed on 16/10/2017.
 */

public final class LocalDatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LocalDatabaseContract(){ }

    /* Inner class that defines the table contents */
    public static class GroupsEntry implements BaseColumns {
        public static final String TABLE_NAME = "groups";
        public static final String COLUMN_NAME_GROUP_NAME = "groupName";
        public static final String COLUMN_NAME_USER_RT = "registrationToken";
        public static final String COLUMN_NAME_USER_NAME = "name";
    }
}
