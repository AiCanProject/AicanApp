package com.aican.aicanapp.FirebaseAccounts;

public class DeviceAccount {
    public String api;
    public String app;
    public String database;
    public String project;

    public DeviceAccount() {
    }

    public DeviceAccount(String api, String app, String database, String project) {
        this.api = api;
        this.app = app;
        this.database = database;
        this.project = project;
    }
}
