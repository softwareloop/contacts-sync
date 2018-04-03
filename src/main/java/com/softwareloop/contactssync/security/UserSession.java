package com.softwareloop.contactssync.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
public class UserSession implements Serializable {
    final String userId;
    final String displayName;
    final String email;
    final String picture;
    final String csrfToken;
}
