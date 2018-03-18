package com.softwareloop.contactssync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareloop.contactssync.security.IdTokenPayload;
import com.softwareloop.contactssync.security.JwtToken;
import com.softwareloop.contactssync.security.TokenResponse;
import com.softwareloop.contactssync.security.UserSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Base64;
import java.util.Random;

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
    // Fields
    //--------------------------------------------------------------------------

    private final Random random = new Random();

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

    @Autowired
    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Interface implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    @RequestMapping("/login")
    public String login(
            HttpSession httpSession,
            @RequestParam(
                    value = POST_AUTH_REDIRECT_ATTRIBUTE,
                    required = false
            ) String postAuthRedirect
    ) {
        httpSession.setAttribute(
                POST_AUTH_REDIRECT_ATTRIBUTE, postAuthRedirect);

        // Generate Oauth2 state
        String oauth2State = generateRandomString(16);
        httpSession.setAttribute(OAUTH2_STATE_ATTRIBUTE, oauth2State);

        // Build authorization URI
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(googleUserAuthorizationUri)
                .queryParam("access_type", "offline")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri",
                        "http://localhost:8080/google-login-callback")
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", oauth2State);

        return "redirect:" + uriBuilder.toUriString();
    }

    @RequestMapping("/google-login-callback")
    public String login(
            @RequestParam("code") String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam("state") String state,
            @SessionAttribute(
                    value = POST_AUTH_REDIRECT_ATTRIBUTE,
                    required = false
            ) String postAuthRedirect,
            @SessionAttribute(OAUTH2_STATE_ATTRIBUTE) String expectedState,
            HttpSession httpSession
    ) throws Exception {
        httpSession.removeAttribute(POST_AUTH_REDIRECT_ATTRIBUTE);
        httpSession.removeAttribute(OAUTH2_STATE_ATTRIBUTE);

        if (error != null) {
            throw new Exception("Error");
        }

        if (!state.equals(expectedState)) {
            throw new IllegalArgumentException("OAuth2 state mismatch");
        }

        // Exchange the code for a authorisation/refresh tokens
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
        TokenResponse tokenResponse = response.getBody();

        String accessToken = tokenResponse.getAccess_token();
        String refreshToken = tokenResponse.getRefresh_token();

        // Extract the OpenId payload
        String idToken = tokenResponse.getId_token();
        JwtToken jwtToken = JwtToken.fromTokenString(idToken);
        IdTokenPayload idTokenPayload = new ObjectMapper()
                .readValue(jwtToken.getPayload(), IdTokenPayload.class);
        String userId = idTokenPayload.getSub();

        // Set the user session properties
        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setDisplayName(idTokenPayload.getName());
        userSession.setEmail(idTokenPayload.getEmail());
        userSession.setPicture(idTokenPayload.getPicture());
        httpSession.setAttribute(USER_SESSION_ATTRIBUTE, userSession);

        if (postAuthRedirect != null) {
            return "redirect:" + postAuthRedirect;
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping("/logout")
    public String logout(
            HttpSession httpSession,
            UserSession userSession
    ) {
        httpSession.invalidate();
        return "redirect:/";
    }

    public String generateRandomString(int entropyBytes) {
        byte[] buffer = new byte[entropyBytes];
        random.nextBytes(buffer);
        return new String(Base64.getEncoder().encode(buffer));
    }
}