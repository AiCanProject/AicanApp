package com.aican.aicanappnoncfr.FirebaseAccounts;

public class DeviceAccount {
    public String api;
    public String app;
    public String database;
    public String project;
    public String type;

    public DeviceAccount() {
    }

    public DeviceAccount(String api, String app, String database, String project, String type) {
        this.api = api;
        this.app = app;
        this.database = database;
        this.project = project;
        this.type = type;
    }
}
