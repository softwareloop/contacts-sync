package com.softwareloop.contactssync.util;

public class TextUtils {

    //--------------------------------------------------------------------------
    // Static methods
    //--------------------------------------------------------------------------

    public static String escapeJsForInlineScript(String text) {
        return text.replace("/", "\\/");
    }

}
