package com.softwareloop.contactssync.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
public class UserSession implements Serializable {
    @NotNull
    final String userId;

    final String displayName;

    final String email;

    final String picture;

    @NotNull
    final String csrfToken;



}
