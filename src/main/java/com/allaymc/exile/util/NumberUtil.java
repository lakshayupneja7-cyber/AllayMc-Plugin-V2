package com.allaymc.exile.util;

import java.util.Locale;

public final class NumberUtil {
    private NumberUtil() {}
    public static String stripDecimal(double value) {
        if (value == Math.floor(value)) return String.valueOf((int) value);
        return String.format(Locale.US, "%.2f", value);
    }
}
