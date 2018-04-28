package com.softwareloop.contactssync.google;

import com.softwareloop.contactssync.model.google.Person;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConnectionsResponse {
    private final List<Person> connections = new ArrayList<>();
    private String nextPageToken;
    private String nextSyncToken;
    private String totalPeople;
    private String totalItems;
}
