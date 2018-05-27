package com.softwareloop.contactssync.controllers;

import com.softwareloop.contactssync.google.GmailSynchronizer;
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

    private final GmailSynchronizer gmailSynchronizer;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public SyncController(
            GmailSynchronizer gmailSynchronizer
    ) {
        this.gmailSynchronizer = gmailSynchronizer;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    @PostMapping("/sync")
    public SimpleResponse sync(
            UserSession userSession
    ) throws IOException {
        gmailSynchronizer.syncContacts(userSession.getUserId());
        return new SimpleResponse("Ok");
    }
}
