package com.softwareloop.contactssync.google;

import com.google.api.services.people.v1.model.*;
import com.softwareloop.contactssync.model.Email;
import com.softwareloop.contactssync.model.GooglePerson;
import com.softwareloop.contactssync.model.Phone;
import com.softwareloop.contactssync.util.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GmailModelHelper {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public GooglePerson createGooglePerson(@NotNull Person person) {
        GooglePerson googlePerson = new GooglePerson();
        googlePerson.setResourceName(person.getResourceName());
        List<Name> googleNames = person.getNames();
        if (googleNames == null || googleNames.isEmpty()) {
            return null;
        }

        Name googleName = googleNames.get(0);
        googlePerson.setGivenName(googleName.getGivenName());
        googlePerson.setFamilyName(googleName.getFamilyName());

        List<Email> emails = new ArrayList<>();
        googlePerson.setEmails(emails);
        List<EmailAddress> googleEmails = person.getEmailAddresses();
        if (googleEmails == null) {
            googleEmails = Collections.emptyList();
        }
        for (EmailAddress googleEmail : googleEmails) {
            Email email = new Email();
            email.setAddress(googleEmail.getValue());
            email.setType(googleEmail.getType());
            emails.add(email);
        }

        List<Phone> phones = new ArrayList<>();
        googlePerson.setPhones(phones);
        List<PhoneNumber> googlePhones = person.getPhoneNumbers();
        if (googlePhones == null) {
            googlePhones = Collections.emptyList();
        }
        for (PhoneNumber googlePhone : googlePhones) {
            Phone phone = new Phone();
            phone.setNumber(googlePhone.getValue());
            phone.setType(googlePhone.getType());
            phones.add(phone);
        }

        List<Photo> googlePhotos = person.getPhotos();
        if (googlePhotos == null) {
            googlePhotos = Collections.emptyList();
        }
        String photoUrl = null;
        for (Photo googlePhoto : googlePhotos) {
            if (BooleanUtils.isTrue(googlePhoto.getDefault())) {
                photoUrl = googlePhoto.getUrl();
            }
        }
        if (photoUrl == null && !googlePhotos.isEmpty()) {
            photoUrl = googlePhotos.get(0).getUrl();
        }
        googlePerson.setPhotoUrl(photoUrl);

        return googlePerson;
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
