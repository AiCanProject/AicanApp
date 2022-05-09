package com.aican.aicanappnoncfr.dataClasses;

public class PumpDevice {
    String id;
    String name;
    int mode;
    int speed;
    int dir;
    Integer vol;
    int status;

    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public PumpDevice(String id, String name, int mode, int speed, int dir, Integer vol, int status) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.speed = speed;
        this.dir = dir;
        this.vol = vol;
        this.status = status;
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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
