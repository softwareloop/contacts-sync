package com.softwareloop.contactssync.model;

import lombok.Data;

import java.util.List;

@Data
public class GooglePerson {
    String resourceName;
    List<Name> names;
}
