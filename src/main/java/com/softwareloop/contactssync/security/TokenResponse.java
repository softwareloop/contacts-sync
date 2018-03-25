package com.softwareloop.contactssync.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("refresh_token")
    String refreshToken;

    @JsonProperty("expires_in")
    int expiresIn;

    @JsonProperty("id_token")
    String idToken;

    @JsonProperty("token_type")
    String tokenType;
}
