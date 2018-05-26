package com.softwareloop.contactssync.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongojack.JacksonCodecRegistry;

import javax.annotation.PostConstruct;
import java.io.Serializable;

public abstract class AbstractDao<K extends Serializable, V extends Serializable> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final MongoDatabase mongoDatabase;
    private final Class<V> objectType;
    protected MongoCollection<V> collection;
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AbstractDao(
            MongoDatabase mongoDatabase,
            Class<V> objectType
    ) {
        this.mongoDatabase = mongoDatabase;
        this.objectType = objectType;
    }

    @PostConstruct
    public void init() {
        JacksonCodecRegistry jacksonCodecRegistry = new JacksonCodecRegistry();
        jacksonCodecRegistry.addCodecForClass(objectType);
        collection = mongoDatabase
                .getCollection(objectType.getSimpleName())
                .withDocumentClass(objectType)
                .withCodecRegistry(jacksonCodecRegistry);
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------

    @Nullable
    public V getById(@NotNull K id) {
        Document query = new Document("_id", id);
        return collection.find(query).first();
    }

    public void insert(@NotNull V obj) {
        collection.insertOne(obj);
    }

    public void update(V obj) {
        K id = getId(obj);
        Document query = new Document("_id", id);
        collection.replaceOne(query, obj);
    }

    public void delete(V obj) {
        K id = getId(obj);
        deleteById(id);
    }

    public void deleteById(K id) {
        Document query = new Document("_id", id);
        collection.deleteOne(query);
    }

    public long count() {
        return collection.count();
    }

    public abstract K getId(V obj);

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
