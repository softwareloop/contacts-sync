package com.softwareloop.contactssync.security;

import lombok.Data;

@Data
public class IdTokenPayload {
    String iss;
    String at_hash;
    String email_verified;
    String sub;
    String azp;
    String email;
    String aud;
    String iat;
    String exp;
    String nonce;
    String hd;
    String name;
    String picture;
    String given_name;
    String family_name;
    String locale;
}
