package com.aican.aicanappnoncfr.dataClasses;

public class BufferData {
    String ph;
    String mv;
    String time;

    public BufferData() {
    }


    public BufferData(String ph, String mv, String time) {
        this.ph = ph;
        this.mv = mv;
        this.time = time;
    }

    public BufferData(String ph) {
        this.ph = ph;
    }

    public BufferData( String mv, String time) {
        this.mv = mv;
        this.time = time;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getMv() {
        return mv;
    }

    public void setMv(String mv) {
        this.mv = mv;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
