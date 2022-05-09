package com.aican.aicanappnoncfr.dataClasses;

public class TempDevice {
    String id;
    String name;
    int temp;

    public TempDevice(String id, String name, int temp) {
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

    public void setName(String name) {
        this.name = name;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
