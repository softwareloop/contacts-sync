package com.softwareloop.contactssync.model;

import lombok.Data;

import java.util.List;

@Data
public class GooglePerson {
    String resourceName;
    String photoUrl;
    String givenName;
    String familyName;
    List<Email> emails;
    List<Phone> phones;
}
