package com.notifyapp.amitbed.notifyme;

import java.util.ArrayList;

/**
 * Created by amitbed on 13/11/2017.
 */

public interface IFirebaseDbHandler {
    ArrayList<String> getGroupsNames();
    void createNewGroup();

}
