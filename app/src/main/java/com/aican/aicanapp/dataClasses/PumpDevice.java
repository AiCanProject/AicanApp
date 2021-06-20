package com.aican.aicanapp.dataClasses;

import androidx.annotation.Nullable;

public class PumpDevice {
    String id;
    String name;
    int mode;
    int speed;
    int dir;
    Integer vol;

    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public PumpDevice(String id, String name, int mode, int speed, int dir,@Nullable Integer vol) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.speed = speed;
        this.dir = dir;
        this.vol = vol;
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
}
