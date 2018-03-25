package com.softwareloop.contactssync.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IdTokenPayload {

    String iss;

    @JsonProperty("at_hash")
    String atHash;

    @JsonProperty("email_verified")
    String emailVerified;

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

    @JsonProperty("given_name")
    String givenName;

    @JsonProperty("family_name")
    String familyName;

    String locale;
}
