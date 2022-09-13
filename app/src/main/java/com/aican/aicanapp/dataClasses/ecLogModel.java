package com.aican.aicanapp.dataClasses;

public class ecLogModel {
    String date;
    String time;
    String conductivity;
    String TDS;
    String temperature;
    String batchNum;
    String productName;

    public ecLogModel(String date,String time,String conductivity,String TDS, String temperature, String batchNum,String productName){
        this.date = date;
        this.time = time;
        this.conductivity = conductivity;
        this.TDS = TDS;
        this.temperature = temperature;
        this.batchNum = batchNum;
        this.productName = productName;

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

    public String getConductivity() {
        return conductivity;
    }

    public void setConductivity(String conductivity) {
        this.conductivity = conductivity;
    }

    public String getTDS() {
        return TDS;
    }

    public void setTDS(String TDS) {
        this.TDS = TDS;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
