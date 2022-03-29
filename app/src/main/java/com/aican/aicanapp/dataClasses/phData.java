package com.aican.aicanapp.dataClasses;

public class phData {
    String pH;
    String mV;
    String date;
    String compound_name;

    public phData(String pH, String mV, String date, String compound_name) {
        this.pH = pH;
        this.mV = mV;
        this.date = date;
        this.compound_name = compound_name;
    }

    public String getCompound_name() {
        return compound_name;
    }

    public void setCompound_name(String compound_name) {
        this.compound_name = compound_name;
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
