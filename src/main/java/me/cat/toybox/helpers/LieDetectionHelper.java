package me.cat.toybox.helpers;

public class LieDetectionHelper {

    public static boolean isInt(Object value) {
        try {
            Integer.parseInt(String.valueOf(value));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
