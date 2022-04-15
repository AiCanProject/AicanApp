package com.aican.aicanapp.dataClasses;

public class PumpDevice {
    String id;
    String name;
    Integer mode;
    Integer speed;
    Integer dir;
    Integer vol;
    Integer status;


    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public PumpDevice(String id, String name, int mode, int speed, int dir, int vol, int status) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.speed = speed;
        this.dir = dir;
        this.vol = vol;
        this.status = status;
    }

    public PumpDevice(String id, String name, int speed, int direction, int volume) {
        this.id = id;
        this.name = name;
        this.speed =speed;
        this.dir = direction;
        this.vol = volume;
    }

    public PumpDevice(String id, String name) {
        this.id = id;
        this.name = name;
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
