package com.notifyapp.amitbed.notifyme;

/**
 * Created by amitbed on 07/10/2017.
 */

public class User {
    private String phoneNumber;
    private String registrationToken;
    private String userId;

    public User(String userId, String phoneNumber, String registrationToken){
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.registrationToken = registrationToken;
    }

    public User(){
        this.userId = null;
        this.phoneNumber = null;
        this.registrationToken = null;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
