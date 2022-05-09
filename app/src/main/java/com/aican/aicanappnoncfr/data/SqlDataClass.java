package com.aican.aicanappnoncfr.data;

public class SqlDataClass {
    String userId;
    String passCode;
    String name;
    String role;

    public SqlDataClass(String userId, String passCode, String name) {
        this.userId = userId;
        this.passCode = passCode;
        this.name = name;
        //this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole(){
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
