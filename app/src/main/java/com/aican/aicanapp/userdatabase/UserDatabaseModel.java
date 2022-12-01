package com.aican.aicanapp.userdatabase;

public class UserDatabaseModel {
    String user_name;
    String passcode;
    String id;
    String user_role;
    String expiry_date;
    String dateCreated;


    public UserDatabaseModel(String user_name, String passcode, String id, String user_role, String expiry_date, String dateCreated) {
        this.user_name = user_name;
        this.passcode = passcode;
        this.id = id;
        this.user_role = user_role;
        this.expiry_date = expiry_date;
        this.dateCreated = dateCreated;
    }


    public String getUser_name() {
        return user_name;
    }

    public String getUser_role() {
        return user_role;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser_role(String user_role) {
        this.user_role = user_role;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
