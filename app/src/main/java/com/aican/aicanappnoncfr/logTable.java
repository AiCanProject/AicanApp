package com.aican.aicanappnoncfr;

public class logTable {

    private String date;
    private String pH;
    private String mV;

    public logTable(String date, String pH, String mV) {
        this.date = date;
        this.pH = pH;
        this.mV = mV;
    }

    public String getDate() {
        return date;
    }

    public String getpH() {
        return pH;
    }

    public String getmV() {
        return mV;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setpH(String pH) {
        this.pH = pH;
    }

    public void setmV(String mV) {
        this.mV = mV;
    }
}
