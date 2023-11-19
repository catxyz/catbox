package me.cat.testplugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Utils {

    public static String formatNum(Object number) {
        double d = Double.parseDouble(new DecimalFormat("#.#").format(number));
        return NumberFormat.getInstance().format(d);
    }
}
