package com.softwareloop.contactssync.dao;

import com.mongodb.client.MongoDatabase;
import com.softwareloop.contactssync.model.PersonEntity;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersonEntityDao extends AbstractDao<ObjectId, PersonEntity> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public PersonEntityDao(
            MongoDatabase mongoDatabase) {
        super(mongoDatabase, PersonEntity.class);
    }


    //--------------------------------------------------------------------------
    // AbstractDao implementation
    //--------------------------------------------------------------------------

    @Override
    public ObjectId getId(PersonEntity obj) {
        return obj.getId();
    }


    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public List<PersonEntity> getByUserId(String userId) {
        Document query = new Document("userId", userId);
        return collection.find(query).into(new ArrayList<>());
    }

}
