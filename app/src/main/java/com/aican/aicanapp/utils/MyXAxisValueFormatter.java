package com.aican.aicanapp.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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