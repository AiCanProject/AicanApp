package com.aican.aicanapp;

public class UserDatabaseModel {
    String user_name;
    String user_role;

    public UserDatabaseModel(String user_name, String user_role) {
        this.user_name = user_name;
        this.user_role = user_role;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_role() {
        return user_role;
    }
}
