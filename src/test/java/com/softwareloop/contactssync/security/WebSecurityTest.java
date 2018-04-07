package com.softwareloop.contactssync.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareloop.contactssync.ContactsSyncApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import static com.softwareloop.contactssync.security.SecurityConstants.OAUTH2_STATE_ATTRIBUTE;
import static com.softwareloop.contactssync.security.SecurityConstants.USER_SESSION_ATTRIBUTE;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"google.clientId=client_id", "google.clientSecret=client_secret"},
        classes = ContactsSyncApplication.class)
@AutoConfigureMockMvc
public class WebSecurityTest {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String GOOGLE_LOGIN_CALLBACK_URL =
            "/google-login-callback?state=1234&code=4/ABCD&authuser=0&session_state=5678&prompt=none";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;


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
    // Tests: /
    //--------------------------------------------------------------------------

    @Test
    public void testHomeAsAnonymous() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/"))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(
                                "Import your LinkedIn contacts")));
        expectStandardHeaders(resultActions);
    }

    @Test
    public void testHomeAsLoggedIn() throws Exception {
        logInProgrammatically();
        ResultActions resultActions =
                mockMvc.perform(get("/").session(httpSession))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(content().string(""))
                        .andExpect(header().string("Location", "/app"));
        expectStandardHeaders(resultActions);
    }

    //--------------------------------------------------------------------------
    // Tests: /app
    //--------------------------------------------------------------------------

    @Test
    public void testAppAsAnonymous() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/app"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(content().string(""))
                        .andExpect(header().string("Location",
                                "/login?post_auth_redirect=%2Fapp"));
        expectStandardHeaders(resultActions);
    }

    @Test
    public void testAppAsLoggedIn() throws Exception {
        logInProgrammatically();
        ResultActions resultActions =
                mockMvc.perform(get("/app").session(httpSession))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(
                                "{\"userId\":\"1234\",\"displayName\":\"John Doe\",\"email\":\"john.doe@example.com\",\"picture\":null,\"csrfToken\":\"56789\"}")));
        expectStandardHeaders(resultActions);
    }

    //--------------------------------------------------------------------------
    // Tests: /login
    //--------------------------------------------------------------------------

    @Test
    public void testLoginAsAnonymous() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/login?post_auth_redirect=%2Fapp")
                        .session(httpSession))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(content().string(""));

        String state = (String) httpSession.getAttribute(
                SecurityConstants.OAUTH2_STATE_ATTRIBUTE);
        Assert.assertNotNull(state);
        Assert.assertEquals(32, state.length());
        resultActions.andExpect(header().string("Location",
                "https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=client_id&redirect_uri=http://localhost:8080/google-login-callback&response_type=code&scope=openid%20email%20profile&state=" + state));
        expectStandardHeaders(resultActions);

        String postAuthRedirect = (String) httpSession.getAttribute(
                SecurityConstants.POST_AUTH_REDIRECT_ATTRIBUTE);
        Assert.assertEquals("%2Fapp", postAuthRedirect);
    }

    @Test
    public void testLoginAsAnonymousNoRedirect() throws Exception {
        mockMvc.perform(get("/login")
                .session(httpSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(""));

        String postAuthRedirect = (String) httpSession.getAttribute(
                SecurityConstants.POST_AUTH_REDIRECT_ATTRIBUTE);
        Assert.assertNull(postAuthRedirect);
    }

    @Test
    public void testLoginAsLoggedIn() {
        logInProgrammatically();
        try {
            mockMvc.perform(get("/login")
                    .session(httpSession));
            Assert.fail("Exception expected but not thrown");
        } catch (Exception e) {
            Assert.assertEquals("You are already already logged in",
                    NestedExceptionUtils.getRootCause(e).getMessage());
        }
    }

    //--------------------------------------------------------------------------
    // Tests: /google-login-callback
    // http://localhost:8080/google-login-callback?state=1234&code=4/ABCD&authuser=0&session_state=5678&prompt=none
    //--------------------------------------------------------------------------

    @Test
    public void testLoginCallback() throws Exception {
        Assert.assertNull(httpSession.getAttribute(USER_SESSION_ATTRIBUTE));

        IdTokenPayload idTokenPayload = new IdTokenPayload();
        idTokenPayload.setSub("1234567890");
        idTokenPayload.setEmail("peter.white@example.com");
        idTokenPayload.setName("Peter White");
        idTokenPayload.setPicture("Picture URL");
        JwtToken jwtToken = new JwtToken(
                "header",
                objectMapper.writeValueAsString(idTokenPayload),
                null);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setIdToken(jwtToken.toTokenString());
        ResponseEntity response =
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        Mockito.when(restTemplate.postForEntity(
                Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(response);

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
    // Tests: /logout
    //--------------------------------------------------------------------------

    @Test
    public void testLogoutAsAnonymous() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/logout").session(httpSession))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(content().string(""))
                        .andExpect(header().string("Location", "/"));
        expectStandardHeaders(resultActions);
        Assert.assertTrue(httpSession.isInvalid());
    }

    @Test
    public void testLogoutAsLoggedIn() throws Exception {
        logInProgrammatically();
        testLogoutAsAnonymous();
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