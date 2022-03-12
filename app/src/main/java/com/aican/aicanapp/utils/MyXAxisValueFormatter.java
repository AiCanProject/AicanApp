package com.aican.aicanapp.utils;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyXAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        String currentDateAndTime = new SimpleDateFormat("HH:mm").format(new Date());
        return currentDateAndTime;
    }
}