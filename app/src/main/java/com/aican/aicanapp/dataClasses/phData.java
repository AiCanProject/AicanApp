package com.aican.aicanapp.dataClasses;

public class phData {
    String pH;
    String mV;
    String date;
    String time;

    public phData(String pH, String mV) {
        this.pH = pH;
        this.mV = mV;
    }

    public phData() {
    }

    public phData(String currentTime) {
        this.time =currentTime;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getpH() {
        return pH;
    }

    public void setpH(String pH) {
        this.pH = pH;
    }

    public String getmV() {
        return mV;
    }

    public void setmV(String mV) {
        this.mV = mV;
    }

}
