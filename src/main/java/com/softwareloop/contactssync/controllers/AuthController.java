package com.softwareloop.contactssync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareloop.contactssync.security.IdTokenPayload;
import com.softwareloop.contactssync.security.JwtToken;
import com.softwareloop.contactssync.security.TokenResponse;
import com.softwareloop.contactssync.security.UserSession;
import com.softwareloop.contactssync.util.TextUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;

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

    @Getter
    private final ObjectMapper objectMapper;

    @Getter
    private final RestTemplate restTemplate;

    @Getter
    @Setter
    @Value("${google.clientId}")
    private String googleClientId;

    @Getter
    @Setter
    @Value("${google.clientSecret}")
    private String googleClientSecret;

    @Getter
    @Setter
    @Value("${google.userAuthorizationUri}")
    private String googleUserAuthorizationUri;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    @Autowired
    public AuthController(
            ObjectMapper objectMapper,
            RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
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
        return UriComponentsBuilder
                .fromUriString(googleUserAuthorizationUri)
                .queryParam("access_type", "offline")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri",
                        "http://localhost:8080/google-login-callback")
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", oauth2State)
                .toUriString();
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

        TokenResponse tokenResponse = exchangeCodeForTokenResponse(code);

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        IdTokenPayload idTokenPayload = extractIdTokenPayload(tokenResponse);

        userSession = createUserSession(idTokenPayload);
        httpSession.setAttribute(USER_SESSION_ATTRIBUTE, userSession);

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

    private TokenResponse exchangeCodeForTokenResponse(String code) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED.toString());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", "http://localhost:8080/google-login-callback");
        map.add("scope", "openid email profile");
        map.add("client_id", googleClientId);
        map.add("client_secret", googleClientSecret);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(map, headers);
        ResponseEntity<TokenResponse> response =
                restTemplate.postForEntity(
                        "https://www.googleapis.com/oauth2/v4/token",
                        request,
                        TokenResponse.class);
        return response.getBody();
    }

    private IdTokenPayload extractIdTokenPayload(
            TokenResponse tokenResponse
    ) throws java.io.IOException {
        String idToken = tokenResponse.getIdToken();
        JwtToken jwtToken = JwtToken.fromTokenString(idToken);
        return objectMapper
                .readValue(jwtToken.getPayload(), IdTokenPayload.class);
    }

    @NotNull
    private UserSession createUserSession(
            @NotNull IdTokenPayload idTokenPayload
    ) {
        UserSession userSession;
        userSession = new UserSession(
                idTokenPayload.getSub(),
                idTokenPayload.getName(),
                idTokenPayload.getEmail(),
                idTokenPayload.getPicture(),
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
