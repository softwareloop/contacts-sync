package com.softwareloop.contactssync.security;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSession implements Serializable {
    String userId;
    String displayName;
    String email;
    String picture;
}
