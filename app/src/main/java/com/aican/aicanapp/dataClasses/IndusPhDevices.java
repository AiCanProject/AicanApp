package com.aican.aicanapp.dataClasses;

public class IndusPhDevices {
    String id;
    String name;
    float iph;
    float ec;
    int temp;
    int tds;

    public IndusPhDevices() {

    }

    public IndusPhDevices(String id, String name, float ph, float ec) {
        this.id = id;
        this.name = name;
        this.iph = ph;
        this.ec = ec;
    }

    public IndusPhDevices(String id, String name) {
        this.id = id;
        this.name = name;
       // this.ec = ec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPh() {
        return iph;
    }

    public void setPh(float ph) {
        this.iph = ph;
    }

    public float getEc() {
        return ec;
    }

    public void setEc(float ec) {
        this.ec = ec;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTds() {
        return tds;
    }

    public void setTds(int tds) {
        this.tds = tds;
    }
}
