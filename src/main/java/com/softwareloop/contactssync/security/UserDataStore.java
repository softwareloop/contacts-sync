package com.softwareloop.contactssync.security;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.softwareloop.contactssync.dao.UserDao;
import com.softwareloop.contactssync.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class UserDataStore implements DataStore<StoredCredential> {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final UserDao userDao;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public UserDataStore(UserDao userDao) {
        this.userDao = userDao;
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
        return (int) userDao.count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(String key) {
        return userDao.getById(key) != null;
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
    public StoredCredential get(String key) {
        User user = userDao.getById(key);
        if (user == null) {
            return null;
        }

        StoredCredential result = new StoredCredential();
        result.setAccessToken(user.getAccessToken());
        result.setExpirationTimeMilliseconds(user.getExpirationTimeMilliseconds());
        result.setRefreshToken(user.getRefreshToken());
        return result;
    }

    @Override
    public DataStore<StoredCredential> set(String key, StoredCredential value) {
        userDao.updateCredentials(
                key,
                value.getAccessToken(),
                value.getExpirationTimeMilliseconds(),
                value.getRefreshToken());
        return this;
    }

    @Override
    public DataStore<StoredCredential> clear() {
        return null;
    }

    @Override
    public DataStore<StoredCredential> delete(String userId) {
        userDao.deleteById(userId);
        return this;
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
