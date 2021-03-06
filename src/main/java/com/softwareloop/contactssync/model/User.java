package com.softwareloop.contactssync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    @JsonProperty("_id")
    private String userId;
    private String displayName;
    private String email;
    private String picture;
}
