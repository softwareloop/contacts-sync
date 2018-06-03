package com.softwareloop.contactssync.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.softwareloop.contactssync.model.ContactEntity;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContactEntityDao extends AbstractDao<ObjectId, ContactEntity> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public ContactEntityDao(
            MongoDatabase mongoDatabase) {
        super(mongoDatabase, ContactEntity.class);
    }


    //--------------------------------------------------------------------------
    // AbstractDao implementation
    //--------------------------------------------------------------------------

    @Override
    public ObjectId getId(ContactEntity obj) {
        return new ObjectId(obj.getId());
    }


    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    public List<ContactEntity> getByUserId(String userId, Bson sort) {
        Document query = new Document("userId", userId);
        FindIterable<ContactEntity> findIterable = collection.find(query);
        if (sort != null) {
            findIterable = findIterable.sort(sort);
        }
        return findIterable.into(new ArrayList<>());
    }

    public void deleteByUserId(String userId) {
        Document query = new Document("userId", userId);
        collection.deleteMany(query);
    }

}
