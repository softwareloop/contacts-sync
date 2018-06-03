package com.softwareloop.contactssync.util;

public class BooleanUtils {

    public static boolean isTrue(Boolean value) {
        return value != null && value;
    }

    public static boolean isFalse(Boolean value) {
        return value != null && !value;
    }

}
