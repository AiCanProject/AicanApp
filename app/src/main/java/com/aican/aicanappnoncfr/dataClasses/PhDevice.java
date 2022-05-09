package com.aican.aicanappnoncfr.dataClasses;

public class PhDevice {
    String id;
    String name;
    float ph;
    float ec;
    int temp;
    int tds;

    public PhDevice(String id, String name, float ph, float ec, int temp, long tds) {
        this.id = id;
        this.name = name;
        this.ph = ph;
        this.ec = ec;
        this.temp = temp;
        this.tds = (int) Math.min(10000, tds);
    }

    public PhDevice() {
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