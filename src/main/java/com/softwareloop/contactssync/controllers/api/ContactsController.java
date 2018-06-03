package com.softwareloop.contactssync.controllers.api;

import com.softwareloop.contactssync.dao.ContactEntityDao;
import com.softwareloop.contactssync.google.GmailSynchronizer;
import com.softwareloop.contactssync.model.ContactEntity;
import com.softwareloop.contactssync.security.UserSession;
import com.softwareloop.contactssync.util.ContactEntityComparator;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final GmailSynchronizer gmailSynchronizer;
    private final ContactEntityDao contactEntityDao;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public ContactsController(
            GmailSynchronizer gmailSynchronizer,
            ContactEntityDao contactEntityDao) {
        this.gmailSynchronizer = gmailSynchronizer;
        this.contactEntityDao = contactEntityDao;
    }

    //--------------------------------------------------------------------------
    // Endpoints
    //--------------------------------------------------------------------------


    @GetMapping
    public List<ContactEntity> getContacts(
            UserSession userSession
    ) {
        List<ContactEntity> contacts =
                contactEntityDao.getByUserId(userSession.getUserId(), null);
        contacts.sort(new ContactEntityComparator());
        return contacts;
    }

    @PostMapping("/sync")
    public SimpleResponse sync(
            UserSession userSession
    ) throws IOException {
        gmailSynchronizer.syncContacts(userSession.getUserId());
        return new SimpleResponse("Ok");
    }
}
