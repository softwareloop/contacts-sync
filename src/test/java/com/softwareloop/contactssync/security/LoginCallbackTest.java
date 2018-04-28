package com.softwareloop.contactssync.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.softwareloop.contactssync.ContactsSyncApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;

import static com.softwareloop.contactssync.security.SecurityConstants.OAUTH2_STATE_ATTRIBUTE;
import static com.softwareloop.contactssync.security.SecurityConstants.USER_SESSION_ATTRIBUTE;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"google.clientId=client_id", "google.clientSecret=client_secret"},
        classes = ContactsSyncApplication.class)
@AutoConfigureMockMvc
public class LoginCallbackTest {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String GOOGLE_LOGIN_CALLBACK_URL =
            "/google-login-callback?state=1234&code=4/ABCD&authuser=0&session_state=5678&prompt=none";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonFactory jsonFactory;

    @MockBean
    private AuthorizationCodeFlow flow;

    @Mock
    AuthorizationCodeTokenRequest tokenRequest;

    private UserSession userSession;
    private MockHttpSession httpSession;

    //--------------------------------------------------------------------------
    // Setup
    //--------------------------------------------------------------------------

    @Before
    public void setup() {
        httpSession = new MockHttpSession();
        userSession = new UserSession(
                "1234",
                "John Doe",
                "john.doe@example.com",
                null,
                "56789");
    }

    //--------------------------------------------------------------------------
    // Tests: /google-login-callback
    // http://localhost:8080/google-login-callback?state=1234&code=4/ABCD&authuser=0&session_state=5678&prompt=none
    //--------------------------------------------------------------------------

    @Test
    public void testLoginCallback() throws Exception {
        Assert.assertNull(httpSession.getAttribute(USER_SESSION_ATTRIBUTE));

        IdToken.Header idTokenHeader = new IdToken.Header();
        idTokenHeader.setAlgorithm("RSA");

        IdToken.Payload idTokenPayload = new IdToken.Payload();
        idTokenPayload.setSubject("1234567890");
        idTokenPayload.set("email", "peter.white@example.com");
        idTokenPayload.set("name", "Peter White");
        idTokenPayload.set("picture", "Picture URL");

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        String idTokenString = JsonWebSignature.signUsingRsaSha256(
                privateKey, jsonFactory, idTokenHeader, idTokenPayload);
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setIdToken(idTokenString);

        Mockito.when(flow.newTokenRequest(Mockito.any()))
                .thenReturn(tokenRequest);
        Mockito.when(tokenRequest.setRedirectUri(Mockito.any()))
                .thenReturn(tokenRequest);
        Mockito.when(tokenRequest.execute())
                .thenReturn(tokenResponse);

        httpSession.setAttribute(OAUTH2_STATE_ATTRIBUTE, "1234");
        ResultActions resultActions =
                mockMvc.perform(get(GOOGLE_LOGIN_CALLBACK_URL).session(httpSession))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(content().string(""))
                        .andExpect(header().string("Location", "/"));
        expectStandardHeaders(resultActions);

        UserSession userSession =
                (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
        Assert.assertNotNull(userSession);
        Assert.assertEquals("1234567890", userSession.getUserId());
        Assert.assertEquals("peter.white@example.com", userSession.getEmail());
        Assert.assertEquals("Peter White", userSession.getDisplayName());
        Assert.assertEquals("Picture URL", userSession.getPicture());
        Assert.assertNotNull(userSession.getCsrfToken());
    }

    @Test
    public void testLoginCallbackAsLoggedIn() {
        logInProgrammatically();
        try {
            mockMvc.perform(get(GOOGLE_LOGIN_CALLBACK_URL).session(httpSession));
            Assert.fail("Exception expected but not thrown");
        } catch (Exception e) {
            Assert.assertEquals("You are already already logged in",
                    NestedExceptionUtils.getRootCause(e).getMessage());
        }
    }

    @Test
    public void testLoginCallbackNoOauthState() {
        try {
            mockMvc.perform(get(GOOGLE_LOGIN_CALLBACK_URL).session(httpSession));
            Assert.fail("Exception expected but not thrown");
        } catch (Exception e) {
            Assert.assertEquals("OAuth2 state mismatch",
                    NestedExceptionUtils.getRootCause(e).getMessage());
        }
    }

    @Test
    public void testLoginCallbackWrongOauthState() {
        httpSession.setAttribute(OAUTH2_STATE_ATTRIBUTE, "wrong");
        try {
            mockMvc.perform(get(GOOGLE_LOGIN_CALLBACK_URL).session(httpSession));
            Assert.fail("Exception expected but not thrown");
        } catch (Exception e) {
            Assert.assertEquals("OAuth2 state mismatch",
                    NestedExceptionUtils.getRootCause(e).getMessage());
        }
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    public void expectStandardHeaders(
            ResultActions resultActions
    ) throws Exception {
        resultActions.andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Cache-Control",
                        "no-cache, no-store, max-age=0, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Expires", "0"));
    }

    public void logInProgrammatically() {
        httpSession.setAttribute(
                SecurityConstants.USER_SESSION_ATTRIBUTE, userSession);

    }
}