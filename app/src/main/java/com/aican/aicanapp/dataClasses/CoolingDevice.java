package com.aican.aicanapp.dataClasses;

public class CoolingDevice {

    String id;
    String name;
    int temp;

    public CoolingDevice(String id, String name, int temp) {
        this.id = id;
        this.name = name;
        this.temp = temp;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTemp() {
        return temp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
