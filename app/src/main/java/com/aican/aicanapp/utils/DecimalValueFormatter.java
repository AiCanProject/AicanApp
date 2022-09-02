package com.aican.aicanapp.utils;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Locale;

public class DecimalValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.format(Locale.UK, "%.2f", value);
    }
}