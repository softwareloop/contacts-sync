package com.softwareloop.contactssync.util;

import org.springframework.web.util.WebUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

public class TextUtils {

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final static SecureRandom random = new SecureRandom();

    //--------------------------------------------------------------------------
    // Static methods
    //--------------------------------------------------------------------------

    public static String escapeJsForInlineScript(String text) {
        return text.replace("/", "\\/");
    }

    public static String generate128BitRandomString() {
        byte[] buffer = new byte[16];
        random.nextBytes(buffer);
        return DatatypeConverter.printHexBinary(buffer);
    }

    public static String urlEncode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

}
