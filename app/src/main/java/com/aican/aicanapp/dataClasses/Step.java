package com.aican.aicanapp.dataClasses;

import androidx.annotation.ColorInt;

public class Step {

    @ColorInt
    int bg;

    public Step(int bg) {
        this.bg = bg;
    }

    public int getBg() {
        return bg;
    }
}
