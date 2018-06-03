package com.softwareloop.contactssync.controllers.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.softwareloop.contactssync.dao.ContactEntityDao;
import com.softwareloop.contactssync.dao.DbCredentialDao;
import com.softwareloop.contactssync.dao.UserDao;
import com.softwareloop.contactssync.google.GmailClient;
import com.softwareloop.contactssync.security.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private final AuthorizationCodeFlow flow;
    private final UserDao userDao;
    private final DbCredentialDao dbCredentialDao;
    private final ContactEntityDao contactEntityDao;
    private final GmailClient gmailClient;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public SettingsController(
            AuthorizationCodeFlow flow, UserDao userDao,
            DbCredentialDao dbCredentialDao,
            ContactEntityDao contactEntityDao,
            GmailClient gmailClient
    ) {
        this.flow = flow;
        this.userDao = userDao;
        this.dbCredentialDao = dbCredentialDao;
        this.contactEntityDao = contactEntityDao;
        this.gmailClient = gmailClient;
    }

    //--------------------------------------------------------------------------
    // Endpoints
    //--------------------------------------------------------------------------

    @PostMapping("/disconnect-and-logout")
    public SimpleResponse disconnectAndLogout(
            HttpSession httpSession,
            UserSession userSession
    ) throws IOException {
        String userId = userSession.getUserId();
        Credential credential = flow.loadCredential(userId);
        gmailClient.deauthorize(credential);

        contactEntityDao.deleteByUserId(userId);
        userDao.deleteById(userId);
        dbCredentialDao.deleteById(userId);

        httpSession.invalidate();

        return new SimpleResponse("Ok");
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

}
