package com.softwareloop.contactssync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DbCredential implements Serializable {
    @JsonProperty("_id")
    private String userId;
    private String accessToken;
    private Long expirationTimeMilliseconds;
    private String refreshToken;
}
