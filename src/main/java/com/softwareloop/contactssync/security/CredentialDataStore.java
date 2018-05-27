package com.softwareloop.contactssync.security;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.softwareloop.contactssync.dao.DbCredentialDao;
import com.softwareloop.contactssync.model.DbCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class CredentialDataStore implements DataStore<StoredCredential> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final DbCredentialDao dbCredentialDao;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public CredentialDataStore(DbCredentialDao dbCredentialDao) {
        this.dbCredentialDao = dbCredentialDao;
    }

    //--------------------------------------------------------------------------
    // DataStore implementations
    //--------------------------------------------------------------------------

    @Override
    public DataStoreFactory getDataStoreFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return (int) dbCredentialDao.count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(String userId) {
        return dbCredentialDao.getById(userId) != null;
    }

    @Override
    public boolean containsValue(StoredCredential value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<StoredCredential> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoredCredential get(String userId) {
        DbCredential dbCredentialDaoById = dbCredentialDao.getById(userId);
        if (dbCredentialDaoById == null) {
            return null;
        }

        StoredCredential result = new StoredCredential();
        result.setAccessToken(dbCredentialDaoById.getAccessToken());
        result.setExpirationTimeMilliseconds(dbCredentialDaoById.getExpirationTimeMilliseconds());
        result.setRefreshToken(dbCredentialDaoById.getRefreshToken());
        return result;
    }

    @Override
    public DataStore<StoredCredential> set(
            String userId,
            StoredCredential storedCredential) {
        DbCredential dbCredential = dbCredentialDao.getById(userId);
        boolean newCredential = false;
        if (dbCredential == null) {
            newCredential = true;
            dbCredential = new DbCredential();
        }

        dbCredential.setUserId(userId);
        dbCredential.setAccessToken(storedCredential.getAccessToken());
        dbCredential.setExpirationTimeMilliseconds(
                storedCredential.getExpirationTimeMilliseconds());
        String refreshToken = storedCredential.getRefreshToken();
        if (refreshToken != null) {
            dbCredential.setRefreshToken(refreshToken);
        }

        if (newCredential) {
            dbCredentialDao.insert(dbCredential);
        } else {
            dbCredentialDao.update(dbCredential);
        }

        return this;
    }

    @Override
    public DataStore<StoredCredential> clear() {
        return null;
    }

    @Override
    public DataStore<StoredCredential> delete(String userId) {
        dbCredentialDao.deleteById(userId);
        return this;
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
