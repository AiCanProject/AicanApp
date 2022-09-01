package com.aican.aicanapp.userdatabase;

public class UserDatabaseModel {
    String user_name;
    String user_role;
    String expiry_date;
    String dateCreated;
    public UserDatabaseModel(String user_name, String user_role,String expiry_date,String dateCreated) {
        this.user_name = user_name;
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
}
