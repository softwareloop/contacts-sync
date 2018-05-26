package com.softwareloop.contactssync.dao;

import com.mongodb.client.MongoDatabase;
import com.softwareloop.contactssync.model.User;
import org.bson.Document;
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

    public void updateCredentials(
            String userId,
            String accessToken,
            Long expirationTimeMilliseconds,
            String refreshToken
    ) {
        Document query = new Document("_id", userId);
        Document setFields = new Document();
        setFields.put("accessToken", accessToken);
        setFields.put("expirationTimeMilliseconds", expirationTimeMilliseconds);
        if (refreshToken != null) {
            setFields.put("refreshToken", refreshToken);
        }
        Document update = new Document("$set", setFields);
        collection.updateOne(query, update);
    }

}
