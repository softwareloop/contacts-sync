package com.softwareloop.contactssync.util;

import com.softwareloop.contactssync.model.ContactEntity;

import java.util.Comparator;

public class ContactEntityComparator implements Comparator<ContactEntity> {

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
    // Comparator implementation
    //--------------------------------------------------------------------------

    @Override
    public int compare(ContactEntity o1, ContactEntity o2) {
        String familyName1 = o1.getFamilyName();
        String familyName2 = o2.getFamilyName();
        if (familyName1 == null) {
            if (familyName2 == null) {
                // wait to compare given names
            } else {
                return 1;
            }
        } else {
            if (familyName2 == null) {
                return -1;
            } else {
                return familyName1.compareToIgnoreCase(familyName2);
            }
        }

        String givenName1 = o1.getGivenName();
        String givenName2 = o2.getGivenName();
        if (givenName1 == null) {
            if (givenName2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (givenName2 == null) {
                return -1;
            } else {
                return givenName1.compareToIgnoreCase(givenName2);
            }
        }
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
