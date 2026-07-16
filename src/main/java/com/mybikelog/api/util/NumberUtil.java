package com.mybikelog.api.util;

public final class NumberUtil {

    private NumberUtil() {}

    public static double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
