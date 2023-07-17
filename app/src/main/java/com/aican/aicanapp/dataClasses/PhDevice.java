package com.aican.aicanapp.dataClasses;

public class PhDevice {
    String id;
    String name;
    float ph;
    float ec;
    int temp;
    int tds;
    int offline;

    public PhDevice(String id, String name, float ph, float ec, int temp, long tds, int offline) {
        this.id = id;
        this.name = name;
        this.ph = ph;
        this.ec = ec;
        this.temp = temp;
        this.offline    = offline;
        this.tds = (int) Math.min(10000, tds);
    }

    public PhDevice() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOffline() {
        return offline;
    }

    public void setOffline(int offline) {
        this.offline = offline;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPh() {
        return ph;
    }

    public float getEc() {
        return ec;
    }

    public int getTemp() {
        return temp;
    }

    public int getTds() {
        return tds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPh(float ph) {
        this.ph = ph;
    }

    public void setEc(float ec) {
        this.ec = ec;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public void setTds(int tds) {
        this.tds = tds;
    }

    public void setTds(long tds) {
        this.tds = (int) Math.min(10000, tds);
    }
}
