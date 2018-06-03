package com.softwareloop.contactssync.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.people.v1.model.Person;
import com.softwareloop.contactssync.dao.ContactEntityDao;
import com.softwareloop.contactssync.model.ContactEntity;
import com.softwareloop.contactssync.model.GooglePerson;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class GmailSynchronizer {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final AuthorizationCodeFlow flow;
    private final GmailClient gmailService;
    private final GmailModelHelper gmailModelHelper;
    private final ContactEntityDao personEntityDao;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public GmailSynchronizer(
            AuthorizationCodeFlow flow,
            GmailClient gmailService,
            GmailModelHelper gmailModelHelper, ContactEntityDao personEntityDao
    ) {
        this.flow = flow;
        this.gmailService = gmailService;
        this.gmailModelHelper = gmailModelHelper;
        this.personEntityDao = personEntityDao;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public void syncContacts(String userId) throws IOException {
        Credential credential = flow.loadCredential(userId);

        List<ContactEntity> personEntities =
                personEntityDao.getByUserId(userId, null);
        Map<String, ContactEntity> personEntitiesMap =
                new HashMap<>(personEntities.size());
        for (ContactEntity personEntity : personEntities) {
            GooglePerson googlePerson = personEntity.getGooglePerson();
            if (googlePerson == null) {
                continue;
            }
            String resourceName = googlePerson.getResourceName();
            personEntitiesMap.put(resourceName, personEntity);
        }

        List<Person> contacts =
                gmailService.getContacts(credential);

        for (Person person: contacts) {
            String resourceName = person.getResourceName();
            ContactEntity personEntity = personEntitiesMap.get(resourceName);
            if (personEntity == null) {
                // new person: add
                personEntity = createPersonEntity(userId, person);
                personEntityDao.insert(personEntity);
            } else {
                // existing person: update
                updatePersonEntity(personEntity, person);
                personEntityDao.update(personEntity);
            }
            personEntitiesMap.remove(resourceName);
        }

        // Process any remaining entities
        for (ContactEntity personEntity : personEntitiesMap.values()) {
            personEntity.setGooglePerson(null);
            personEntityDao.delete(personEntity);
        }
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    private ContactEntity createPersonEntity(
            String userId,
            @NotNull Person person
    ) {
        ContactEntity personEntity = new ContactEntity();
        personEntity.setUserId(userId);
        GooglePerson googlePerson =
                gmailModelHelper.createGooglePerson(person);
        personEntity.setGooglePerson(googlePerson);
        updateNames(personEntity);
        return personEntity;
    }

    private void updateNames(ContactEntity personEntity) {
        if (updateNamesFromGoogle(personEntity)) {
            return;
        }
        // TODO: try with Linked In
    }

    private boolean updateNamesFromGoogle(ContactEntity personEntity) {
        GooglePerson googlePerson = personEntity.getGooglePerson();
        if (googlePerson == null) {
            return false;
        }
        personEntity.setGivenName(googlePerson.getGivenName());
        personEntity.setFamilyName(googlePerson.getFamilyName());
        return true;
    }


    private void updatePersonEntity(ContactEntity personEntity, Person person) {
        GooglePerson googlePerson =
                gmailModelHelper.createGooglePerson(person);
        personEntity.setGooglePerson(googlePerson);
        updateNames(personEntity);
    }


}
