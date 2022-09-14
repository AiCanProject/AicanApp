package com.aican.aicanapp.dataClasses;

public class tempData {
    String temp1, temp2, set_temp, date, time, batchnum, product_name;

    public tempData(String date, String time, String set_temp, String temp1, String temp2, String product_name, String batchnum) {
        this.temp1 = temp1;
        this.temp2 = temp2;
        this.set_temp = set_temp;
        this.date = date;
        this.time = time;
        this.batchnum = batchnum;
        this.product_name = product_name;
    }

    public String getTemp1() {
        return temp1;
    }

    public void setTemp1(String temp1) {
        this.temp1 = temp1;
    }

    public String getTemp2() {
        return temp2;
    }

    public void setTemp2(String temp2) {
        this.temp2 = temp2;
    }

    public String getSet_temp() {
        return set_temp;
    }

    public void setSet_temp(String set_temp) {
        this.set_temp = set_temp;
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

    public String getBatchnum() {
        return batchnum;
    }

    public void setBatchnum(String batchnum) {
        this.batchnum = batchnum;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }
}
