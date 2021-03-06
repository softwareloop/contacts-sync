package com.softwareloop.contactssync.dao;

import com.mongodb.client.MongoDatabase;
import com.softwareloop.contactssync.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDao extends AbstractDao<String, User> {

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
    public UserDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase, User.class);
    }

    //--------------------------------------------------------------------------
    // AbstractDao implementation
    //--------------------------------------------------------------------------

    @Override
    public String getId(User obj) {
        return obj.getUserId();
    }


    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

}
