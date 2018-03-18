package com.softwareloop.contactssync.security;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
public class JwtToken {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder();
    public static final Base64.Decoder BASE_64_DECODER = Base64.getUrlDecoder();

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private String header;
    private String payload;
    private byte[] signature;

    //--------------------------------------------------------------------------
    // Constructors and builders
    //--------------------------------------------------------------------------

    public JwtToken() {
    }

    public JwtToken(String header, String payload, byte[] signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    public static JwtToken fromTokenString(String token) {
        String[] idTokenParts = token.split("\\.");
        String header = decode(idTokenParts[0]);
        String body = decode(idTokenParts[1]);
        byte[] signature;
        if (idTokenParts.length > 2) {
            signature = BASE_64_DECODER.decode(idTokenParts[2]);
        } else {
            signature = null;
        }
        return new JwtToken(header, body, signature);
    }

    //--------------------------------------------------------------------------
    // Interface implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public String toTokenString() {
        StringBuilder sb = new StringBuilder();
        sb.append(encode(header));
        sb.append(".");
        sb.append(encode(payload));
        if (signature != null) {
            sb.append(".");
            sb.append(BASE_64_ENCODER.encodeToString(signature));
        }
        return sb.toString();
    }

    private String encode(String text) {
        return BASE_64_ENCODER.encodeToString(
                text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        byte[] bytes = BASE_64_DECODER.decode(text);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}