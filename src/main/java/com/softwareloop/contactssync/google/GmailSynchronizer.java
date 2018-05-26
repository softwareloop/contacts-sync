package com.softwareloop.contactssync.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.people.v1.model.Person;
import com.softwareloop.contactssync.dao.PersonEntityDao;
import com.softwareloop.contactssync.model.GooglePerson;
import com.softwareloop.contactssync.model.Name;
import com.softwareloop.contactssync.model.PersonEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final PersonEntityDao personEntityDao;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public GmailSynchronizer(
            AuthorizationCodeFlow flow,
            GmailClient gmailService,
            PersonEntityDao personEntityDao
    ) {
        this.flow = flow;
        this.gmailService = gmailService;
        this.personEntityDao = personEntityDao;
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public void syncContacts(String userId) throws IOException {
        Credential credential = flow.loadCredential(userId);

        List<PersonEntity> personEntities = personEntityDao.getByUserId(userId);
        Map<String, PersonEntity> personEntitiesMap =
                new HashMap<>(personEntities.size());
        for (PersonEntity personEntity : personEntities) {
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
            PersonEntity personEntity = personEntitiesMap.get(resourceName);
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
        for (PersonEntity personEntity : personEntitiesMap.values()) {
            personEntity.setGooglePerson(null);
            personEntityDao.delete(personEntity);
        }
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    private PersonEntity findPersonEntityByPerson(
            Map<String, PersonEntity> personEntitiesMap,
            Person person) {
        String resourceName = person.getResourceName();
        return personEntitiesMap.get(resourceName);
    }

    private PersonEntity createPersonEntity(
            String userId,
            @NotNull Person person
    ) {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setUserId(userId);
        GooglePerson googlePerson = new GooglePerson();
        googlePerson.setResourceName(person.getResourceName());
        personEntity.setGooglePerson(googlePerson);
        List<Name> names = new ArrayList<>();
        googlePerson.setNames(names);
        List<com.google.api.services.people.v1.model.Name> googleNames =
                person.getNames();
        if (googleNames == null) {
            return personEntity;
        }
        for (com.google.api.services.people.v1.model.Name googleName : googleNames) {
            Name name = new Name();
            name.setGivenName(googleName.getGivenName());
            name.setFamilyName(googleName.getFamilyName());
            names.add(name);
        }
        return personEntity;
    }


    private void updatePersonEntity(PersonEntity personEntity, Person person) {
        // TODO
    }


}
