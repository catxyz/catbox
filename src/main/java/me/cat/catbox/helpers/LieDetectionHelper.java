package me.cat.catbox.helpers;

import java.util.Arrays;
import java.util.Objects;

public class LieDetectionHelper {

    public static boolean isInt(Object value) {
        try {
            Integer.parseInt(String.valueOf(value));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static <E> boolean arrayHasNull(E[] arr) {
        return Arrays.stream(arr).anyMatch(Objects::isNull);
    }
}
