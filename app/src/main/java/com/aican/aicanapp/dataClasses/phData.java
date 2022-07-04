package com.aican.aicanapp.dataClasses;

public class phData {
    String pH;
    String mV;
    String date;
    String compound_name;
    String time;
    String batchnum;
    String arnum;

    public phData(String pH, String mV, String date, String time, String batchnum, String arnum, String compound_name) {
        this.pH = pH;
        this.mV = mV;
        this.date = date;
        this.time = time;
        this.batchnum = batchnum;
        this.arnum = arnum;
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

    public String getTime(){
        return time;
    }

    public String getBatchnum(){return batchnum;}

    public String getArnum(){return arnum;
    }


    public void setBatchnum(String batchnum){this.batchnum = batchnum;}
    public void setArnum(String arnum){this.arnum = arnum;}


    public void setTime(String time){this.time = time;}
    public void setDate(String date) {
        this.date = date;
    }
}
