package com.softwareloop.contactssync.controllers;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.softwareloop.contactssync.security.UserSession;
import com.softwareloop.contactssync.util.TextUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.softwareloop.contactssync.security.SecurityConstants.*;

/**
 * Documentation:
 * https://developers.google.com/identity/protocols/OAuth2
 * https://developers.google.com/identity/protocols/OpenIDConnect
 */
@Slf4j
@Controller
public class AuthController {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Variables
    //--------------------------------------------------------------------------

    @Getter
    private final AuthorizationCodeFlow flow;

    @Getter
    private final JacksonFactory jacksonFactory;

    private String redirectUri = "http://localhost:8080/google-login-callback";

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public AuthController(
            AuthorizationCodeFlow flow,
            JacksonFactory jacksonFactory
    ) {
        this.flow = flow;
        this.jacksonFactory = jacksonFactory;
    }

    //--------------------------------------------------------------------------
    // Interface implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Login
    //--------------------------------------------------------------------------

    @RequestMapping("/login")
    public String login(
            HttpSession httpSession,
            UserSession userSession,
            @RequestParam(
                    value = POST_AUTH_REDIRECT_ATTRIBUTE,
                    required = false
            ) String postAuthRedirect
    ) {
        checkUserNotLoggedIn(userSession);

        httpSession.setAttribute(
                POST_AUTH_REDIRECT_ATTRIBUTE, postAuthRedirect);

        String oauth2State = TextUtils.generate128BitRandomString();
        httpSession.setAttribute(OAUTH2_STATE_ATTRIBUTE, oauth2State);

        String authorizationUri = createAuthorizationUri(oauth2State);
        return "redirect:" + authorizationUri;
    }

    private String createAuthorizationUri(String oauth2State) {
        AuthorizationCodeRequestUrl authorizationUrl =
                flow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(redirectUri);
        authorizationUrl.setState(oauth2State);
        return authorizationUrl.build();
    }

    //--------------------------------------------------------------------------
    // Google login callback
    //--------------------------------------------------------------------------

    @RequestMapping("/google-login-callback")
    public String googleLoginCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam("state") String state,
            @SessionAttribute(
                    value = POST_AUTH_REDIRECT_ATTRIBUTE,
                    required = false
            ) String postAuthRedirect,
            @SessionAttribute(
                    value = OAUTH2_STATE_ATTRIBUTE,
                    required = false
            ) String expectedState,
            HttpSession httpSession,
            UserSession userSession
    ) throws Exception {
        checkUserNotLoggedIn(userSession);

        httpSession.removeAttribute(POST_AUTH_REDIRECT_ATTRIBUTE);
        httpSession.removeAttribute(OAUTH2_STATE_ATTRIBUTE);

        if (error != null) {
            throw new Exception("Error");
        }

        checkOauth2StateMatches(state, expectedState);

        GoogleTokenResponse googleTokenResponse =
                exchangeCodeForTokenResponse(code);

        IdToken.Payload idTokenPayload =
                extractIdTokenPayload(googleTokenResponse);

        userSession = createUserSession(idTokenPayload);
        httpSession.setAttribute(USER_SESSION_ATTRIBUTE, userSession);

        // Store the credential
        Credential credential =
                flow.createAndStoreCredential(
                        googleTokenResponse, userSession.getUserId());

        if (postAuthRedirect != null) {
            return "redirect:" + postAuthRedirect;
        } else {
            return "redirect:/";
        }
    }

    private void checkOauth2StateMatches(String state, String expectedState) {
        if (!state.equals(expectedState)) {
            throw new IllegalArgumentException("OAuth2 state mismatch");
        }
    }

    private GoogleTokenResponse exchangeCodeForTokenResponse(
            String code
    ) throws IOException {
        return (GoogleTokenResponse)
                flow.newTokenRequest(code)
                        .setRedirectUri(redirectUri)
                        .execute();
    }

    private IdToken.Payload extractIdTokenPayload(
            GoogleTokenResponse tokenResponse
    ) throws java.io.IOException {
        String idTokenString = tokenResponse.getIdToken();
        IdToken idToken = IdToken.parse(jacksonFactory, idTokenString);
        return idToken.getPayload();
    }

    @NotNull
    private UserSession createUserSession(
            @NotNull IdToken.Payload idTokenPayload
    ) {
        UserSession userSession;
        userSession = new UserSession(
                idTokenPayload.getSubject(),
                (String) idTokenPayload.get("name"),
                (String) idTokenPayload.get("email"),
                (String) idTokenPayload.get("picture"),
                TextUtils.generate128BitRandomString());
        return userSession;
    }

    //--------------------------------------------------------------------------
    // Logout
    //--------------------------------------------------------------------------

    @RequestMapping("/logout")
    public String logout(
            HttpSession httpSession,
            UserSession userSession
    ) {
        httpSession.invalidate();
        return "redirect:/";
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    private void checkUserNotLoggedIn(UserSession userSession) {
        if (userSession != null) {
            throw new IllegalArgumentException(
                    "You are already already logged in");
        }
    }


}
