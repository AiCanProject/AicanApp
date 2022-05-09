package com.aican.aicanappnoncfr.utils;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class MyXAxisValueFormatter extends IndexAxisValueFormatter {
    public String getFormattedValue(float value) {
        if (value % 125 == 0) {
            int second = (int) value / 25; // get second from value
            return second + "s"; //make it a string and return
        } else {
            return ""; // return empty for other values where you don't want to print anything on the X Axis
        }

    }
}