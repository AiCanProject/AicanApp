package com.aican.aicanappnoncfr.dataClasses;

public class EcDevice {
    String id;
    String name;
    int ec;

    public EcDevice(String id, String name, int ec) {
        this.id = id;
        this.name = name;
        this.ec = ec;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getEc() {
        return ec;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEc(int ec) {
        this.ec = ec;
    }
}
