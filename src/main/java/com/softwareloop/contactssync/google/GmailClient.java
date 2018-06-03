package com.softwareloop.contactssync.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.sun.jndi.toolkit.url.Uri;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GmailClient {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final NetHttpTransport netHttpTransport;
    private final JacksonFactory jacksonFactory;
    private final RestTemplate restTemplate;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public GmailClient(
            NetHttpTransport netHttpTransport,
            JacksonFactory jacksonFactory,
            RestTemplate restTemplate) {
        this.netHttpTransport = netHttpTransport;
        this.jacksonFactory = jacksonFactory;
        this.restTemplate = restTemplate;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public List<Person> getContacts(
            Credential credential
    ) throws IOException {
        PeopleService peopleService =
                new PeopleService.Builder(
                        netHttpTransport,
                        jacksonFactory,
                        credential)
                        .setApplicationName("Contacts sync").build();
        List<Person> allConnections = new ArrayList<>();
        String nextPageToken = null;
        do {
            PeopleService.People.Connections.List connectionsRequest =
                    peopleService.people()
                            .connections()
                            .list("people/me")
                            .setPersonFields("names,emailAddresses,phoneNumbers,photos")
                            .setPageToken(nextPageToken);
            ListConnectionsResponse response =
                    connectionsRequest.execute();
            List<Person> connections = response.getConnections();
            if (connections != null) {
                allConnections.addAll(connections);
            }
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);
        return allConnections;
    }

    public void deauthorize(Credential credential) {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("token", credential.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "https://accounts.google.com/o/oauth2/revoke",
                requestEntity,
                String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new InternalError(responseEntity.getBody());
        }
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
