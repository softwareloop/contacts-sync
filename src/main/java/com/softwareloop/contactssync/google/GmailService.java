package com.softwareloop.contactssync.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GmailService {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final NetHttpTransport netHttpTransport;
    private final JacksonFactory jacksonFactory;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public GmailService(
            NetHttpTransport netHttpTransport,
            JacksonFactory jacksonFactory
    ) {
        this.netHttpTransport = netHttpTransport;
        this.jacksonFactory = jacksonFactory;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public List<Person> getContacts(Credential credential) throws IOException {
        PeopleService peopleService =
                new PeopleService.Builder(
                        netHttpTransport,
                        jacksonFactory,
                        credential)
                        .setApplicationName("Contacts sync").build();
        ListConnectionsResponse response = peopleService.people()
                .connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses")
                .execute();
        return response.getConnections();
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
