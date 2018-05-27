package com.softwareloop.contactssync.dao;

import com.mongodb.client.MongoDatabase;
import com.softwareloop.contactssync.model.DbCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbCredentialDao extends AbstractDao<String, DbCredential> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public DbCredentialDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase, DbCredential.class);
    }

    //--------------------------------------------------------------------------
    // AbstractDao implementation
    //--------------------------------------------------------------------------

    @Override
    public String getId(DbCredential obj) {
        return obj.getUserId();
    }


    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

}
