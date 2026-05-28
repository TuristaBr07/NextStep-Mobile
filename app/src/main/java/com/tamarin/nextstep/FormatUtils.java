package com.tamarin.nextstep;

import java.text.NumberFormat;
import java.util.Locale;

public final class FormatUtils {
    private static final Locale PT_BR = new Locale("pt", "BR");

    private FormatUtils() {}

    public static String currency(double value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }
}
