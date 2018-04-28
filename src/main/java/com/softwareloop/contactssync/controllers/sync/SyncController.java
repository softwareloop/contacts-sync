package com.softwareloop.contactssync.controllers.sync;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.softwareloop.contactssync.controllers.SimpleResponse;
import com.softwareloop.contactssync.google.GmailService;
import com.softwareloop.contactssync.security.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final AuthorizationCodeFlow flow;
    private final GmailService gmailService;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public SyncController(
            AuthorizationCodeFlow flow,
            GmailService gmailService
    ) {
        this.flow = flow;
        this.gmailService = gmailService;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    @PostMapping("/sync")
    public SimpleResponse sync(
            UserSession userSession
    ) throws IOException {
        Credential credential = flow.loadCredential(userSession.getUserId());
        gmailService.getContacts(credential);
        return new SimpleResponse("Ok");
    }
}
