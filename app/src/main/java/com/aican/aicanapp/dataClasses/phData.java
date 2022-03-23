package com.aican.aicanapp.dataClasses;

public class phData {
    String pH;
    String mV;
    String date;

    public phData(String pH, String mV, String date) {
        this.pH = pH;
        this.mV = mV;
        this.date = date;
    }

    public phData() {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
