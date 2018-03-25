package com.softwareloop.contactssync.security;

import org.junit.Assert;
import org.junit.Test;

public class JwtTokenTest {

    private static final String SAMPLE_HEADER = "Sample header";
    private static final String SAMPLE_PAYLOAD = "àèìòù";
    private static final byte[] SAMPLE_SIGNATURE = {20, -45};

    private static final String SAMPLE_TOKEN_STRING =
            "U2FtcGxlIGhlYWRlcg==.w6DDqMOsw7LDuQ==.FNM=";
    private static final String SAMPLE_TOKEN_STRING_NO_SIGNATURE =
            "U2FtcGxlIGhlYWRlcg==.w6DDqMOsw7LDuQ==";

    @Test
    public void testFromTokenString() {
        JwtToken jwtToken = JwtToken.fromTokenString(SAMPLE_TOKEN_STRING);
        Assert.assertEquals(SAMPLE_HEADER, jwtToken.getHeader());
        Assert.assertEquals(SAMPLE_PAYLOAD, jwtToken.getPayload());
        Assert.assertArrayEquals(SAMPLE_SIGNATURE, jwtToken.getSignature());
    }

    @Test
    public void testFromTokenStringNoSignature() {
        JwtToken jwtToken =
                JwtToken.fromTokenString(SAMPLE_TOKEN_STRING_NO_SIGNATURE);
        Assert.assertEquals(SAMPLE_HEADER, jwtToken.getHeader());
        Assert.assertEquals(SAMPLE_PAYLOAD, jwtToken.getPayload());
        Assert.assertNull(jwtToken.getSignature());
    }

    @Test
    public void testToTokenString() {
        JwtToken jwtToken = new JwtToken(
                SAMPLE_HEADER, SAMPLE_PAYLOAD, SAMPLE_SIGNATURE);
        Assert.assertEquals(SAMPLE_TOKEN_STRING, jwtToken.toTokenString());
    }

    @Test
    public void testToTokenStringNoSignature() {
        JwtToken jwtToken = new JwtToken(
                SAMPLE_HEADER, SAMPLE_PAYLOAD, null);
        Assert.assertEquals(
                SAMPLE_TOKEN_STRING_NO_SIGNATURE, jwtToken.toTokenString());
    }
}