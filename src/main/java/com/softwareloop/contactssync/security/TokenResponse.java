package com.softwareloop.contactssync.security;

import lombok.Data;

@Data
public class TokenResponse {
    String access_token;
    String refresh_token;
    int expires_in;
    String id_token;
    String token_type;
}
